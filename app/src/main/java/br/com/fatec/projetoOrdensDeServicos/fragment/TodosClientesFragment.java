package br.com.fatec.projetoOrdensDeServicos.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.adapter.ClienteAdapter;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaListarServicoCliente;
import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaMenuAdmin;
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class TodosClientesFragment extends Fragment {
    private ArrayList<Cliente> clientes;
    private ClienteAdapter clienteAdapter;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private String getNome;
    private String getTelefone;
    private String usuarioID;
    private TextInputLayout txtInputLayout1, txtInputLayout2;
    private TextInputEditText txtNomeDialog, txtTelDialog;
    private Dialog alterarPerfil;
    private Cliente cliente;
    private ClienteAdapter.StatusContaClickListener statusContaClickListener;
    private ClienteAdapter.EditarClienteClickListener editarClienteClickListener;
    private ClienteAdapter.ListarServicoClickListener listarServicoClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        RecyclerView rVConsultar = v.findViewById(R.id.rVListarCliente);
        rVConsultar.setHasFixedSize(true);
        rVConsultar.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        db = FirebaseFirestore.getInstance();
        clientes = new ArrayList<>();
        editarContaClick();
        statusContaClick();
        setOnClickListener();
        clienteAdapter = new ClienteAdapter(this.getActivity(), clientes, statusContaClickListener,
                editarClienteClickListener, listarServicoClickListener);
        rVConsultar.setAdapter(clienteAdapter);
        listarMundancaEvento();
        return v;
    }

    private void setOnClickListener() {
        listarServicoClickListener = (v, position) ->
                db.collection("usuarios")
                        .whereEqualTo("email", clientes.get(position).getEmail()).limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        Objects.requireNonNull(task.getResult())) {
                                    usuarioID = document.getId();
                                }
                                Intent intent = new Intent(this.getActivity(),
                                        TelaListarServicoCliente.class);
                                intent.putExtra("nomeCliente", clientes.get(position).getNome());
                                intent.putExtra("usuarioID", usuarioID);
                                Log.d("VER:", usuarioID);
                                startActivity(intent);
                            } else {
                                Log.d("ERROR:", "Error getting documents: ",
                                        task.getException());
                            }
                        });
    }


    private void statusContaClick() {
        statusContaClickListener = (v, position) ->
                db.collection("usuarios")
                        .whereEqualTo("email", clientes.get(position).getEmail()).limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        Objects.requireNonNull(task.getResult())) {
                                    usuarioID = document.getId();
                                }
                                DocumentReference docRef = db.collection("usuarios")
                                        .document(usuarioID);
                                if (clientes.get(position).getStatusConta().equalsIgnoreCase(
                                        "Bloqueado")) {
                                    clientes.get(position).setStatusConta("Desbloqueado");
                                    docRef.update("statusConta", clientes.get(position)
                                            .getStatusConta());
                                } else if (clientes.get(position).getStatusConta()
                                        .equalsIgnoreCase("Desbloqueado")) {
                                    clientes.get(position).setStatusConta("Bloqueado");
                                    docRef.update("statusConta", clientes.get(position)
                                            .getStatusConta());
                                }
                                clienteAdapter.notifyItemChanged(position);
                            } else {
                                Log.d("ERROR:", "Error getting documents: ",
                                        task.getException());
                            }
                        });

    }

    private void editarContaClick() {
        editarClienteClickListener = (v, position) ->
                db.collection("usuarios")
                        .whereEqualTo("email", clientes.get(position).getEmail()).limit(1)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        Objects.requireNonNull(task.getResult())) {
                                    usuarioID = document.getId();
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        this.requireActivity());
                                View view = getLayoutInflater().inflate(R.layout.dialog_editar_conta,
                                        null);
                                builder.setTitle("Editar conta:");
                                builder.setView(view);
                                txtInputLayout1 = view.findViewById(R.id.txtInputLayout1);
                                txtNomeDialog = view.findViewById(R.id.txtNomeDialog);
                                txtInputLayout2 = view.findViewById(R.id.txtInputLayout2);
                                txtTelDialog = view.findViewById(R.id.txtTelDialog);
                                pegarDados();
                                Button btnAtualizar = view.findViewById(R.id.btnAtualizar);
                                TextView txtCancelar = view.findViewById(R.id.txtCancelar);
                                alterarPerfil = builder.create();
                                alterarPerfil.create();
                                btnAtualizar.setOnClickListener(vOne -> verificarCampos(position));
                                txtCancelar.setOnClickListener(vTwo -> alterarPerfil.dismiss());
                                alterarPerfil.setCancelable(false);
                                alterarPerfil.show();
                            } else {
                                Log.d("ERROR:", "Error getting documents: ",
                                        task.getException());
                            }
                        });
    }

    private void pegarDados() {
        txtTelDialog.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL, txtTelDialog));
        docRef = db.collection("usuarios").document(usuarioID);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                txtNomeDialog.setText(documentSnapshot.getString("nome"));
                txtTelDialog.setText(documentSnapshot.getString("telefone"));
                getNome = documentSnapshot.getString("nome");
                getTelefone = documentSnapshot.getString("telefone");
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void verificarCampos(int position) {
        String nome = Objects.requireNonNull(txtNomeDialog.getText()).toString().trim();
        String telefone = Objects.requireNonNull(txtTelDialog.getText()).toString().trim();
        cliente = new Cliente(nome, clientes.get(position).getEmail(), telefone,
                clientes.get(position).getStatusConta());
        if (cliente.getNome().isEmpty() || cliente.getTelefone().isEmpty()) {
            if (cliente.getNome().isEmpty())
                txtInputLayout1.setError("Preencha o campo");
            else
                txtInputLayout1.setError(null);
            if (cliente.getTelefone().isEmpty())
                txtInputLayout2.setError("Preencha o campo");
            else
                txtInputLayout2.setError(null);
        } else {
            if (cliente.getTelefone().length() < 15) {
                Toast.makeText(this.getActivity(), "Telefone Inválido",
                        Toast.LENGTH_LONG).show();
            } else {
                if (cliente.getNome().equals(getNome) && cliente.getTelefone().equals(getTelefone)) {
                    Toast.makeText(this.getActivity(), "Altere nome ou telefone do perfil",
                            Toast.LENGTH_LONG).show();
                } else {
                    atualizarDados(position);
                }
            }
        }
    }

    private void atualizarDados(int position) {
        docRef.update("nome", cliente.getNome());
        docRef.update("telefone", cliente.getTelefone());
        Toast.makeText(this.getActivity(), "Atualizado com sucesso",
                Toast.LENGTH_LONG).show();
        clientes.set(position, cliente);
        clienteAdapter.notifyItemChanged(position);
        alterarPerfil.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEvento() {
        db.collection("usuarios").orderBy("nome", Query.Direction.ASCENDING)
                .whereEqualTo("privilegio", "Cliente")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        assert value != null;
                        if (value.isEmpty()) {
                            Toast.makeText(this.getActivity(), "Nenhum cliente cadastrado!!",
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
