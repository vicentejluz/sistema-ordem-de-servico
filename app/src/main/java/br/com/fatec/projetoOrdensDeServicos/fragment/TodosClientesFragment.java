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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.adapter.ClienteAdapter;
import br.com.fatec.projetoOrdensDeServicos.databinding.DialogEditarContaBinding;
import br.com.fatec.projetoOrdensDeServicos.databinding.FragmentRecyclerviewBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaListarServicoCliente;
import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaMenuAdmin;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class TodosClientesFragment extends Fragment {
    private ArrayList<Cliente> clientes;
    private ClienteAdapter clienteAdapter;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String getNome;
    private String getTelefone;
    private String usuarioID;
    FragmentRecyclerviewBinding binding;
    private DialogEditarContaBinding dialogBinding;
    private Dialog alterarPerfil;
    private Cliente cliente;
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
        listarServicoClickListener = (v, position) ->
                db.collection(Constante.USUARIOS)
                        .whereEqualTo(Constante.EMAIL, clientes.get(position).getEmail()).limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        Objects.requireNonNull(task.getResult())) {
                                    usuarioID = document.getId();
                                }
                                Intent intent = new Intent(this.getActivity(),
                                        TelaListarServicoCliente.class);
                                intent.putExtra(Constante.NOME_CLIENTE, clientes.get(position)
                                        .getNome());
                                intent.putExtra(Constante.USUARIO_ID, usuarioID);
                                startActivity(intent);
                            } else {
                                Log.d(Constante.TAG_ERRO, Constante.ERRO_OBTER_DOCUMENTO,
                                        task.getException());
                            }
                        });
    }


    private void statusContaClick() {
        statusContaClickListener = (v, position) ->
                db.collection(Constante.USUARIOS)
                        .whereEqualTo(Constante.EMAIL, clientes.get(position).getEmail()).limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        Objects.requireNonNull(task.getResult())) {
                                    usuarioID = document.getId();
                                }
                                DocumentReference docRef = db.collection(Constante.USUARIOS)
                                        .document(usuarioID);
                                if (clientes.get(position).getStatusConta().equalsIgnoreCase(
                                        Constante.BLOQUEADO)) {
                                    clientes.get(position).setStatusConta(Constante.DESBLOQUEADO);
                                    docRef.update(Constante.STATUS_CONTA, clientes.get(position)
                                            .getStatusConta());
                                } else if (clientes.get(position).getStatusConta()
                                        .equalsIgnoreCase(Constante.DESBLOQUEADO)) {
                                    clientes.get(position).setStatusConta(Constante.BLOQUEADO);
                                    docRef.update(Constante.STATUS_CONTA, clientes.get(position)
                                            .getStatusConta());
                                }
                                clienteAdapter.notifyItemChanged(position);
                            } else {
                                Log.d(Constante.TAG_ERRO, Constante.ERRO_OBTER_DOCUMENTO,
                                        task.getException());
                            }
                        });

    }

    private void editarContaClick() {
        editarClienteClickListener = (v, position) ->
                db.collection(Constante.USUARIOS)
                        .whereEqualTo(Constante.EMAIL, clientes.get(position).getEmail()).limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        Objects.requireNonNull(task.getResult())) {
                                    usuarioID = document.getId();
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        this.requireActivity());
                                dialogBinding = DialogEditarContaBinding.inflate(
                                        getLayoutInflater());
                                View view = dialogBinding.getRoot();
                                builder.setTitle(Constante.EDITAR_CONTA_DOIS_PONTOS);
                                builder.setView(view);
                                pegarDados();
                                alterarPerfil = builder.create();
                                alterarPerfil.create();
                                dialogBinding.btnAtualizar.setOnClickListener(vOne ->
                                        verificarCampos(position));
                                dialogBinding.btnCancelar.setOnClickListener(vTwo ->
                                        alterarPerfil.dismiss());
                                alterarPerfil.setCancelable(false);
                                alterarPerfil.show();
                            } else {
                                Log.d(Constante.TAG_ERRO, Constante.ERRO_OBTER_DOCUMENTO,
                                        task.getException());
                            }
                        });
    }

    private void pegarDados() {
        dialogBinding.txtTelDialog.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL,
                dialogBinding.txtTelDialog));
        docRef = db.collection(Constante.USUARIOS).document(usuarioID);
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
        String nome = Objects.requireNonNull(dialogBinding.txtNomeDialog.getText()).toString()
                .trim();
        String telefone = Objects.requireNonNull(dialogBinding.txtTelDialog.getText()).toString()
                .trim();
        cliente = new Cliente(nome, clientes.get(position).getEmail(), telefone,
                clientes.get(position).getStatusConta());
        if (cliente.getNome().isEmpty() || cliente.getTelefone().isEmpty()) {
            if (cliente.getNome().isEmpty())
                dialogBinding.txtInputLayout1.setError(Constante.PREENCHA_CAMPO);
            else
                dialogBinding.txtInputLayout1.setError(null);
            if (cliente.getTelefone().isEmpty())
                dialogBinding.txtInputLayout2.setError(Constante.PREENCHA_CAMPO);
            else
                dialogBinding.txtInputLayout2.setError(null);
        } else {
            if (cliente.getTelefone().length() < 15) {
                Toast.makeText(this.getActivity(), Constante.TELEFONE_INVALIDO,
                        Toast.LENGTH_LONG).show();
            } else {
                if (cliente.getNome().equals(getNome) && cliente.getTelefone().equals(getTelefone)) {
                    Toast.makeText(this.getActivity(), Constante.ALTERE_NOME_TELEFONE,
                            Toast.LENGTH_LONG).show();
                } else {
                    atualizarDados(position);
                }
            }
        }
    }

    private void atualizarDados(int position) {
        docRef.update(Constante.NOME, cliente.getNome());
        docRef.update(Constante.TELEFONE, cliente.getTelefone());
        Toast.makeText(this.getActivity(), Constante.ATUALIZADO_SUCESSO,
                Toast.LENGTH_LONG).show();
        clientes.set(position, cliente);
        clienteAdapter.notifyItemChanged(position);
        alterarPerfil.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEvento() {
        db.collection(Constante.USUARIOS).orderBy(Constante.NOME, Query.Direction.ASCENDING)
                .whereEqualTo(Constante.PRIVILEGIO, Constante.CLIENTE)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                    } else {
                        if (Objects.requireNonNull(value).isEmpty()) {
                            Toast.makeText(this.getActivity(), Constante.NENHUM_CLIENTE_CADASTRADO,
                                    Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this.getActivity(), TelaMenuAdmin.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    clientes.add(dc.getDocument().toObject(
                                            Cliente.class));
                                    clienteAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
    }
}
