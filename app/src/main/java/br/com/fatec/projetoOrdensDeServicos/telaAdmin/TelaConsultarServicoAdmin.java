package br.com.fatec.projetoOrdensDeServicos.telaAdmin;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaChat;
import br.com.fatec.projetoOrdensDeServicos.adapter.OrdemServicoAdminAdapter;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityConsultarBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;

public class TelaConsultarServicoAdmin extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<OrdemServico> ordemServicos;
    private ArrayList<String> clientes, usuarioID, getStatus;
    private ArrayList<Double> getPrecos;
    private ActivityConsultarBinding binding;
    private final Locale LOCALE = new Locale("pt", "BR");
    private OrdemServicoAdminAdapter ordemServicoAdminAdapter;
    private boolean isStatus;
    private Double preco = 0.0, getPreco = 0.0;
    private String ordemServicoID, statusServico;
    private final String IDCHAT = "pb6IdWjCKogMvZlnpH4bl13lCM22AD";
    private OrdemServicoAdminAdapter.OrdemServicoAdminClickListener ordemServicoAdminClickListener;
    private OrdemServicoAdminAdapter.ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener;
    private OrdemServicoAdminAdapter.ChatServicoClickListener chatServicoClickListener;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsultarBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.bNVStatus.getMenu().clear();
        binding.bNVStatus.inflateMenu(R.menu.menu_item_bottom_servico);
        binding.rVConsultar.setHasFixedSize(true);
        binding.rVConsultar.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        ordemServicos = new ArrayList<>();
        clientes = new ArrayList<>();
        usuarioID = new ArrayList<>();
        getPrecos = new ArrayList<>();
        getStatus = new ArrayList<>();
        setOnClickListener();
        confirmarExcluirOrderServico();
        chatServico();
        ordemServicoAdminAdapter = new OrdemServicoAdminAdapter(this, ordemServicos, clientes,
                ordemServicoAdminClickListener, excluirOrdemServicoClickListener,
                chatServicoClickListener);
        binding.rVConsultar.setAdapter(ordemServicoAdminAdapter);

        binding.bNVStatus.setOnItemSelectedListener(item -> {
            limparListas();
            setarVisibilidadeTextView(View.VISIBLE);
            switch (item.getItemId()) {
                case R.id.itmTodos:
                    isStatus = true;
                    listarMundancaEvento();
                    break;
                case R.id.itmAberta:
                    listarMundancaEventoStatus(StatusOrdemServico.ABERTA.name());
                    break;
                case R.id.itmCancelada:
                    setarVisibilidadeTextView(View.GONE);
                    listarMundancaEventoStatus(StatusOrdemServico.CANCELADA.name());
                    break;
                case R.id.itmFinalizada:
                    isStatus = false;
                    listarMundancaEventoStatus(StatusOrdemServico.FINALIZADA.name());
                    break;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bNVStatus.setSelectedItemId(R.id.itmTodos);
    }

    private void limparListas() {
        ordemServicos.clear();
        clientes.clear();
        getPrecos.clear();
        getStatus.clear();
    }

    private void setarVisibilidadeTextView(int visibilidade) {
        binding.tVTotalPreco.setVisibility(visibilidade);
        binding.txtTotalPreco.setVisibility(visibilidade);
    }

    private void confirmarExcluirOrderServico() {
        excluirOrdemServicoClickListener = (v, position) -> {
            AlertDialog.Builder confirmaExclusao = new AlertDialog.Builder(this);
            confirmaExclusao.setTitle("Atenção!!");
            confirmaExclusao.setMessage("Tem certeza que deseja excluir: " +
                    ordemServicos.get(position).getNomeServico() + "?");
            confirmaExclusao.setPositiveButton("Sim", (dialogInterface, i) ->
                    excluirOrderServico(position));
            confirmaExclusao.setNegativeButton("Não", null);
            confirmaExclusao.setCancelable(false);
            confirmaExclusao.create().show();
        };
    }

    private void chatServico() {
        chatServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaChat.class);
            intent.putExtra("nomeServico", ordemServicos.get(position).getNomeServico());
            intent.putExtra("cliente", clientes.get(position));
            intent.putExtra("usuarioID", usuarioID.get(position));
            startActivity(intent);
        };
    }

    private void setOnClickListener() {
        ordemServicoAdminClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaInformacaoServicoAdmin.class);
            intent.putExtra("nomeServico", ordemServicos.get(position).getNomeServico());
            intent.putExtra("usuarioID", usuarioID.get(position));
            startActivity(intent);
        };
    }

    private void listarMundancaEvento() {
        db.collection("usuarios")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        for (DocumentChange dc : Objects.requireNonNull(value)
                                .getDocumentChanges()) {
                            statusServico = null;
                            adicionarValores(dc);
                        }
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void adicionarValores(@NonNull DocumentChange dtc) {
        db.collection("usuarios").document(dtc.getDocument().getId())
                .collection("ordensDeServicos").orderBy("nomeServico",
                Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        procuraServico(Objects.requireNonNull(value), dtc);
                    }
                });
    }

    private void listarMundancaEventoStatus(String status) {
        db.collection("usuarios")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        for (DocumentChange dc : Objects.requireNonNull(value)
                                .getDocumentChanges()) {
                            adicionarValoresStatus(dc, status);
                        }
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void adicionarValoresStatus(@NonNull DocumentChange dtc, String status) {
        db.collection("usuarios").document(dtc.getDocument().getId())
                .collection("ordensDeServicos").whereEqualTo("status", status)
                .orderBy("nomeServico", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        statusServico = status;
                        procuraServico(Objects.requireNonNull(value), dtc);
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void procuraServico(@NonNull QuerySnapshot value, DocumentChange dtc) {
        for (DocumentChange dc : value.getDocumentChanges()) {
            if (dc.getType() == DocumentChange.Type.ADDED) {
                ordemServicos.add(dc.getDocument().toObject(
                        OrdemServico.class));
                getStatus.add(dc.getDocument().getString("status"));
                if (statusServico == null) {
                    for (String s : getStatus) {
                        if (s.equals(StatusOrdemServico.ABERTA.name()) ||
                                s.equals(StatusOrdemServico.FINALIZADA.name())) {
                            getPrecos.add(dc.getDocument().getDouble("preco"));
                        }
                    }
                } else {
                    if (!statusServico.equals(StatusOrdemServico.CANCELADA.name()))
                        getPrecos.add(dc.getDocument().getDouble("preco"));
                }

                clientes.add(dtc.getDocument().getString("nome"));
                usuarioID.add(dtc.getDocument().getId());
                getStatus.clear();
            }
            ordemServicoAdminAdapter.notifyDataSetChanged();
        }

        for (Double p : getPrecos)
            preco += p;
        if (preco != null) {
            getPreco = preco;
            binding.txtTotalPreco.setText(NumberFormat.getCurrencyInstance(LOCALE).format(getPreco));
            preco = 0.0;
        }
    }

    private void excluirOrderServico(int position) {
        db.collection("usuarios").document(usuarioID.get(position))
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", ordemServicos.get(position).getNomeServico())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            Log.d("Ver1:", document.getId());
                            ordemServicoID = document.getId();
                        }
                        ExcluirComentario(position);
                        db.collection("usuarios").document(usuarioID.get(position))
                                .collection("ordensDeServicos").document(ordemServicoID)
                                .delete();
                        Toast.makeText(this, "Serviço deletado com sucesso",
                                Toast.LENGTH_LONG).show();
                        getPreco = getPreco - ordemServicos.get(position).getPreco();
                        if(!isStatus)
                            binding.bNVStatus.setSelectedItemId(R.id.itmFinalizada);
                        else
                            binding.bNVStatus.setSelectedItemId(R.id.itmTodos);
                    } else {
                        Log.d("ERROR:", "Erro ao achar documento: ",
                                task.getException());
                    }
                });
    }

    private void ExcluirComentario(int position) {
        db.collection("usuarios").document(usuarioID.get(position))
                .collection("ordensDeServicos")
                .document(ordemServicoID)
                .collection("comentarios")
                .document(usuarioID.get(position))
                .collection(IDCHAT).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documento :
                                Objects.requireNonNull(task.getResult())) {
                            db.collection("usuarios").document(usuarioID.get(position))
                                    .collection("ordensDeServicos")
                                    .document(ordemServicoID)
                                    .collection("comentarios")
                                    .document(usuarioID.get(position))
                                    .collection(IDCHAT)
                                    .document(documento.getId()).delete();
                        }
                    }
                });
        db.collection("usuarios").document(usuarioID.get(position))
                .collection("ordensDeServicos")
                .document(ordemServicoID)
                .collection("comentarios")
                .document(IDCHAT)
                .collection(usuarioID.get(position)).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documento :
                                Objects.requireNonNull(task.getResult())) {
                            db.collection("usuarios").document(usuarioID.get(position))
                                    .collection("ordensDeServicos")
                                    .document(ordemServicoID)
                                    .collection("comentarios")
                                    .document(IDCHAT)
                                    .collection(usuarioID.get(position))
                                    .document(documento.getId()).delete();
                        }
                    }
                });
    }

}