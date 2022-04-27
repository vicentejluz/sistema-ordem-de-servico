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
import br.com.fatec.projetoOrdensDeServicos.util.Constante;
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
            intent.putExtra(Constante.NOME_CLIENTE, clientes.get(position).getNome());
            intent.putExtra(Constante.USUARIO_ID, usuariosID.get(position));
            startActivity(intent);
        };
    }


    private void statusContaClick() {
        statusContaClickListener = (v, position) -> {
            DocumentReference docRef = db.collection(Constante.USUARIOS)
                    .document(usuariosID.get(position));
            if (clientes.get(position).getStatusConta().equalsIgnoreCase(Constante.BLOQUEADO)) {
                clientes.get(position).setStatusConta(Constante.DESBLOQUEADO);
                docRef.update(Constante.STATUS_CONTA, clientes.get(position).getStatusConta());
            } else if (clientes.get(position).getStatusConta()
                    .equalsIgnoreCase(Constante.DESBLOQUEADO)) {
                clientes.get(position).setStatusConta(Constante.BLOQUEADO);
                docRef.update(Constante.STATUS_CONTA, clientes.get(position).getStatusConta());
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
            builder.setTitle(Constante.EDITAR_CONTA_DOIS_PONTOS);
            builder.setView(view);
            pegarDados(position);
            alterarPerfil = builder.create();
            alterarPerfil.create();
            dialogBinding.btnAtualizar.setOnClickListener(vOne -> verificarCampos(position));
            dialogBinding.btnCancelar.setOnClickListener(vTwo -> alterarPerfil.dismiss());
            alterarPerfil.setCancelable(false);
            alterarPerfil.show();
        };
    }

    private void pegarDados(int position) {
        dialogBinding.txtTelDialog.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL,
                dialogBinding.txtTelDialog));
        docRef = db.collection(Constante.USUARIOS).document(usuariosID.get(position));
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                dialogBinding.txtNomeDialog.setText(documentSnapshot.getString(Constante.NOME));
                dialogBinding.txtTelDialog.setText(documentSnapshot.getString(Constante.TELEFONE));
                getNome = documentSnapshot.getString(Constante.NOME);
                getTelefone = documentSnapshot.getString(Constante.TELEFONE);
            }
        });
    }

    private void verificarCampos(int position) {
        nome = Objects.requireNonNull(dialogBinding.txtNomeDialog.getText()).toString().trim();
        telefone = Objects.requireNonNull(dialogBinding.txtTelDialog.getText()).toString().trim();
        cliente = new Cliente(nome, clientes.get(position).getEmail(), telefone,
                clientes.get(position).getStatusConta());
        if (nome.isEmpty() || telefone.isEmpty()) {
            if (nome.isEmpty())
                dialogBinding.txtInputLayout1.setError(Constante.PREENCHA_CAMPO);
            else
                dialogBinding.txtInputLayout1.setError(null);
            if (telefone.isEmpty())
                dialogBinding.txtInputLayout2.setError(Constante.PREENCHA_CAMPO);
            else
                dialogBinding.txtInputLayout2.setError(null);
        } else {
            if (telefone.length() < 15) {
                Toast.makeText(this.getActivity(), Constante.TELEFONE_INVALIDO,
                        Toast.LENGTH_LONG).show();
            } else {
                if (nome.equals(getNome) && telefone.equals(getTelefone)) {
                    Toast.makeText(this.getActivity(), Constante.ALTERE_NOME_TELEFONE,
                            Toast.LENGTH_LONG).show();
                } else {
                    atualizarDados(position);
                }
            }
        }
    }

    private void atualizarDados(int position) {
        docRef.update(Constante.NOME, nome);
        docRef.update(Constante.TELEFONE, telefone);
        Toast.makeText(this.getActivity(), Constante.ATUALIZADO_SUCESSO,
                Toast.LENGTH_LONG).show();
        clientes.set(position, cliente);
        alterarPerfil.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEvento() {
        final String STATUSCONTA;
        if (getArguments() != null) {
            STATUSCONTA = getArguments().getString(Constante.STATUS_CONTA);
            db.collection(Constante.USUARIOS).whereEqualTo(Constante.PRIVILEGIO, Constante.CLIENTE)
                    .orderBy(Constante.NOME, Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                        } else {
                            for (DocumentChange dc : Objects.requireNonNull(value)
                                    .getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    statusContas.add(dc.getDocument().getString(Constante.STATUS_CONTA));
                                    for (String s : statusContas) {
                                        if (s.equals(STATUSCONTA)) {
                                            clientes.add(dc.getDocument().toObject(
                                                    Cliente.class));
                                            usuariosID.add(dc.getDocument().getId());
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
}
