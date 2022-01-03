package br.com.fatec.projetoOrdensDeServicos.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.adapter.ClienteAdapter;
import br.com.fatec.projetoOrdensDeServicos.databinding.DialogEditarContaBinding;
import br.com.fatec.projetoOrdensDeServicos.databinding.FragmentRecyclerviewBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaListarServicoCliente;
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class SelecionarStatusClienteFragment extends Fragment {
    private ArrayList<Cliente> clientes;
    private ClienteAdapter clienteAdapter;
    private FirebaseFirestore db;
    private ArrayList<String> statusContas;
    private ArrayList<String> usuariosID;
    private DocumentReference docRef;
    private String getNome, getTelefone, nome, telefone;
    private DialogEditarContaBinding dialogBinding;
    private Dialog alterarPerfil;
    private Cliente cliente;
    FragmentRecyclerviewBinding binding;
    private ClienteAdapter.StatusContaClickListener statusContaClickListener;
    private ClienteAdapter.EditarClienteClickListener editarClienteClickListener;
    private ClienteAdapter.ListarServicoClickListener listarServicoClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerviewBinding.inflate(inflater, container, false);
        View v = binding.getRoot();
        binding.rVListarCliente.setHasFixedSize(true);
        binding.rVListarCliente.setLayoutManager(new LinearLayoutManager(
                this.getActivity()));
        db = FirebaseFirestore.getInstance();
        clientes = new ArrayList<>();
        usuariosID = new ArrayList<>();
        statusContas = new ArrayList<>();
        editarContaClick();
        statusContaClick();
        setOnClickListener();
        clienteAdapter = new ClienteAdapter(this.getActivity(), clientes, statusContaClickListener,
                editarClienteClickListener, listarServicoClickListener);
        binding.rVListarCliente.setAdapter(clienteAdapter);
        listarMundancaEvento();
        return v;
    }

    private void setOnClickListener() {
        listarServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this.getActivity(), TelaListarServicoCliente.class);
            intent.putExtra("nomeCliente", clientes.get(position).getNome());
            intent.putExtra("usuarioID", usuariosID.get(position));
            Log.d("VER:", usuariosID.get(position));
            startActivity(intent);
        };
    }


    private void statusContaClick() {
        statusContaClickListener = (v, position) -> {
            DocumentReference docRef = db.collection("usuarios")
                    .document(usuariosID.get(position));
            if (clientes.get(position).getStatusConta().equalsIgnoreCase("Bloqueado")) {
                clientes.get(position).setStatusConta("Desbloqueado");
                docRef.update("statusConta", clientes.get(position).getStatusConta());
            } else if (clientes.get(position).getStatusConta()
                    .equalsIgnoreCase("Desbloqueado")) {
                clientes.get(position).setStatusConta("Bloqueado");
                docRef.update("statusConta", clientes.get(position).getStatusConta());
            }
            clientes.remove(position);
            usuariosID.remove(position);
        };

    }

    private void editarContaClick() {
        editarClienteClickListener = (v, position) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.requireActivity());
            dialogBinding = DialogEditarContaBinding.inflate(getLayoutInflater());
            View view = dialogBinding.getRoot();
            builder.setTitle("Editar conta:");
            builder.setView(view);
            pegarDados(position);
            alterarPerfil = builder.create();
            alterarPerfil.create();
            dialogBinding.btnAtualizar.setOnClickListener(vOne -> verificarCampos(position));
            dialogBinding.txtCancelar.setOnClickListener(vTwo -> alterarPerfil.dismiss());
            alterarPerfil.setCancelable(false);
            alterarPerfil.show();
        };
    }

    private void pegarDados(int position) {
        dialogBinding.txtTelDialog.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL,
                dialogBinding.txtTelDialog));
        docRef = db.collection("usuarios").document(usuariosID.get(position));
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                dialogBinding.txtNomeDialog.setText(documentSnapshot.getString("nome"));
                dialogBinding.txtTelDialog.setText(documentSnapshot.getString("telefone"));
                getNome = documentSnapshot.getString("nome");
                getTelefone = documentSnapshot.getString("telefone");
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void verificarCampos(int position) {
        nome = Objects.requireNonNull(dialogBinding.txtNomeDialog.getText()).toString().trim();
        telefone = Objects.requireNonNull(dialogBinding.txtTelDialog.getText()).toString().trim();
        cliente = new Cliente(nome, clientes.get(position).getEmail(), telefone,
                clientes.get(position).getStatusConta());
        if (nome.isEmpty() || telefone.isEmpty()) {
            if (nome.isEmpty())
                dialogBinding.txtInputLayout1.setError("Preencha o campo");
            else
                dialogBinding.txtInputLayout1.setError(null);
            if (telefone.isEmpty())
                dialogBinding.txtInputLayout2.setError("Preencha o campo");
            else
                dialogBinding.txtInputLayout2.setError(null);
        } else {
            if (telefone.length() < 15) {
                Toast.makeText(this.getActivity(), "Telefone Inválido",
                        Toast.LENGTH_LONG).show();
            } else {
                if (nome.equals(getNome) && telefone.equals(getTelefone)) {
                    Toast.makeText(this.getActivity(), "Altere nome ou telefone do perfil",
                            Toast.LENGTH_LONG).show();
                } else {
                    atualizarDados(position);
                }
            }
        }
    }

    private void atualizarDados(int position) {
        docRef.update("nome", nome);
        docRef.update("telefone", telefone);
        Toast.makeText(this.getActivity(), "Atualizado com sucesso",
                Toast.LENGTH_LONG).show();
        clientes.set(position, cliente);
        alterarPerfil.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEvento() {
        assert getArguments() != null;
        final String STATUSCONTA = getArguments().getString("statusConta");
        db.collection("usuarios").whereEqualTo("privilegio", "Cliente")
                .orderBy("nome", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        assert value != null;
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                statusContas.add(dc.getDocument().getString("statusConta"));
                                for (String s : statusContas) {
                                    if (s.equals(STATUSCONTA)) {
                                        clientes.add(dc.getDocument().toObject(
                                                Cliente.class));
                                        usuariosID.add(dc.getDocument().getId());
                                        Log.d("VER", STATUSCONTA);
                                    }
                                }
                                statusContas.clear();
                            }
                        }
                        clienteAdapter.notifyDataSetChanged();
                    }
                });
    }
}
