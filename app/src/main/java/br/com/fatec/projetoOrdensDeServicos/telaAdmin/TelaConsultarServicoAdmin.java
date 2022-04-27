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
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaChat;
import br.com.fatec.projetoOrdensDeServicos.adapter.OrdemServicoAdminAdapter;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityConsultarBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaConsultarServicoAdmin extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<OrdemServico> ordemServicos;
    private ArrayList<String> clientes, usuarioID, getStatus;
    private ArrayList<Double> getPrecos;
    private ActivityConsultarBinding binding;
    private OrdemServicoAdminAdapter ordemServicoAdminAdapter;
    private boolean isStatus;
    private Double preco = 0.0, getPreco = 0.0;
    private String ordemServicoID, statusServico;
    private OrdemServicoAdminAdapter.OrdemServicoAdminClickListener ordemServicoAdminClickListener;
    private OrdemServicoAdminAdapter.ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener;
    private OrdemServicoAdminAdapter.ChatServicoClickListener chatServicoClickListener;

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
            if (item.getItemId() == R.id.itmTodos) {
                isStatus = true;
                listarMundancaEvento();
            } else if (item.getItemId() == R.id.itmAberta) {
                listarMundancaEventoStatus(StatusOrdemServico.ABERTA.name());
            } else if (item.getItemId() == R.id.itmCancelada) {
                setarVisibilidadeTextView(View.GONE);
                listarMundancaEventoStatus(StatusOrdemServico.CANCELADA.name());
            } else {
                isStatus = false;
                listarMundancaEventoStatus(StatusOrdemServico.FINALIZADA.name());
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
            confirmaExclusao.setTitle(Constante.ATENCAO);
            confirmaExclusao.setMessage(Constante.CERTEZA_EXCLUIR +
                    ordemServicos.get(position).getNomeServico() + Constante.PONTO_INTERROGACAO);
            confirmaExclusao.setPositiveButton(Constante.SIM, (dialogInterface, i) ->
                    excluirOrderServico(position));
            confirmaExclusao.setNegativeButton(Constante.NAO, null);
            confirmaExclusao.setCancelable(false);
            confirmaExclusao.create().show();
        };
    }

    private void chatServico() {
        chatServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaChat.class);
            intent.putExtra(Constante.NOME_SERVICO, ordemServicos.get(position).getNomeServico());
            intent.putExtra(Constante.CLIENTE, clientes.get(position));
            intent.putExtra(Constante.USUARIO_ID, usuarioID.get(position));
            startActivity(intent);
        };
    }

    private void setOnClickListener() {
        ordemServicoAdminClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaInformacaoServicoAdmin.class);
            intent.putExtra(Constante.NOME_SERVICO, ordemServicos.get(position).getNomeServico());
            intent.putExtra(Constante.USUARIO_ID, usuarioID.get(position));
            startActivity(intent);
        };
    }

    private void listarMundancaEvento() {
        db.collection(Constante.USUARIOS)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                    } else {
                        for (DocumentChange dc : Objects.requireNonNull(value)
                                .getDocumentChanges()) {
                            statusServico = null;
                            adicionarValores(dc);
                        }
                    }
                });
    }

    private void adicionarValores(@NonNull DocumentChange dtc) {
        db.collection(Constante.USUARIOS).document(dtc.getDocument().getId())
                .collection(Constante.ORDENS_SERVICOS).orderBy(Constante.NOME_SERVICO,
                Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                    } else {
                        procuraServico(Objects.requireNonNull(value), dtc);
                    }
                });
    }

    private void listarMundancaEventoStatus(String status) {
        db.collection(Constante.USUARIOS).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
            } else {
                for (DocumentChange dc : Objects.requireNonNull(value)
                        .getDocumentChanges()) {
                    adicionarValoresStatus(dc, status);
                }
            }
        });
    }

    private void adicionarValoresStatus(@NonNull DocumentChange dtc, String status) {
        db.collection(Constante.USUARIOS).document(dtc.getDocument().getId())
                .collection(Constante.ORDENS_SERVICOS).whereEqualTo(Constante.STATUS, status)
                .orderBy(Constante.NOME_SERVICO, Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
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
                getStatus.add(dc.getDocument().getString(Constante.STATUS));
                if (statusServico == null) {
                    for (String s : getStatus) {
                        if (s.equals(StatusOrdemServico.ABERTA.name()) ||
                                s.equals(StatusOrdemServico.FINALIZADA.name())) {
                            getPrecos.add(dc.getDocument().getDouble(Constante.PRECO));
                        }
                    }
                } else {
                    if (!statusServico.equals(StatusOrdemServico.CANCELADA.name()))
                        getPrecos.add(dc.getDocument().getDouble(Constante.PRECO));
                }

                clientes.add(dtc.getDocument().getString(Constante.NOME));
                usuarioID.add(dtc.getDocument().getId());
                getStatus.clear();
            }
            ordemServicoAdminAdapter.notifyDataSetChanged();
        }

        for (Double p : getPrecos)
            preco += p;
        if (preco != null) {
            getPreco = preco;
            binding.txtTotalPreco.setText(NumberFormat.getCurrencyInstance(Constante.LOCALE)
                    .format(getPreco));
            preco = 0.0;
        }
    }

    private void excluirOrderServico(int position) {
        db.collection(Constante.USUARIOS).document(usuarioID.get(position))
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, ordemServicos.get(position).getNomeServico())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult()))
                            ordemServicoID = document.getId();
                        ExcluirComentario(position);
                        db.collection(Constante.USUARIOS).document(usuarioID.get(position))
                                .collection(Constante.ORDENS_SERVICOS).document(ordemServicoID)
                                .delete();
                        Toast.makeText(this, Constante.SERVICO_DELETADO_SUCESSO,
                                Toast.LENGTH_LONG).show();
                        getPreco = getPreco - ordemServicos.get(position).getPreco();
                        if (!isStatus)
                            binding.bNVStatus.setSelectedItemId(R.id.itmFinalizada);
                        else
                            binding.bNVStatus.setSelectedItemId(R.id.itmTodos);
                    } else {
                        Log.d(Constante.TAG_ERRO, Constante.ERRO_OBTER_DOCUMENTO,
                                task.getException());
                    }
                });
    }

    private void ExcluirComentario(int position) {
        db.collection(Constante.USUARIOS).document(usuarioID.get(position))
                .collection(Constante.ORDENS_SERVICOS)
                .document(ordemServicoID)
                .collection(Constante.COMENTARIO)
                .document(usuarioID.get(position))
                .collection(Constante.IDCHAT).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documento :
                                Objects.requireNonNull(task.getResult())) {
                            db.collection(Constante.USUARIOS).document(usuarioID.get(position))
                                    .collection(Constante.ORDENS_SERVICOS)
                                    .document(ordemServicoID)
                                    .collection(Constante.COMENTARIO)
                                    .document(usuarioID.get(position))
                                    .collection(Constante.IDCHAT)
                                    .document(documento.getId()).delete();
                        }
                    }
                });
        db.collection(Constante.USUARIOS).document(usuarioID.get(position))
                .collection(Constante.ORDENS_SERVICOS)
                .document(ordemServicoID)
                .collection(Constante.COMENTARIO)
                .document(Constante.IDCHAT)
                .collection(usuarioID.get(position)).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documento :
                                Objects.requireNonNull(task.getResult())) {
                            db.collection(Constante.USUARIOS).document(usuarioID.get(position))
                                    .collection(Constante.ORDENS_SERVICOS)
                                    .document(ordemServicoID)
                                    .collection(Constante.COMENTARIO)
                                    .document(Constante.IDCHAT)
                                    .collection(usuarioID.get(position))
                                    .document(documento.getId()).delete();
                        }
                    }
                });
    }

}