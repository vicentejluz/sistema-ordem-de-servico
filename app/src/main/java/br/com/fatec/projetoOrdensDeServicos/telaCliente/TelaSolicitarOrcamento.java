package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivitySolicitarOrcamentoBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaSolicitarOrcamento extends AppCompatActivity implements View.OnClickListener {
    private String nomeServicoID;
    private FirebaseFirestore db;
    private Map<String, Object> data;
    private OrdemServico ordemServico;
    private ActivitySolicitarOrcamentoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivitySolicitarOrcamentoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = FirebaseFirestore.getInstance();
        data = new HashMap<>();
        TooltipCompat.setTooltipText(binding.imBVoltar, Constante.VOLTAR);
        binding.imBVoltar.setOnClickListener(v -> telaMenu());
        binding.btnCadastrar.setOnClickListener(this);
    }

    private void telaMenu() {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        cadastrarOrdemServico();
    }

    public void cadastrarOrdemServico() {
        String nomeServico = Objects.requireNonNull(binding.txtNomeServico.getText()).toString()
                .trim();
        String descricao = Objects.requireNonNull(binding.txtDescricao.getText()).toString().trim();

        ordemServico = new OrdemServico(nomeServico, descricao, Constante.PRECO_ZERO);
        if (ordemServico.getNomeServico().isEmpty() || ordemServico.getDescricao().isEmpty()) {
            Toast.makeText(this, Constante.PREENCHA_TODOS_CAMPOS,
                    Toast.LENGTH_LONG).show();
        } else {
            String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                    .getUid();
            data.put(Constante.NOME_SERVICO, ordemServico.getNomeServico().toLowerCase());
            data.put(Constante.DESCRICAO, ordemServico.getDescricao().toLowerCase());
            data.put(Constante.PRECO, ordemServico.getPreco());
            data.put(Constante.DATA_ABERTURA, ordemServico.getDataAbertura());
            data.put(Constante.DATA_FINALIZACAO, ordemServico.getDatafinalizacao());
            data.put(Constante.STATUS, Constante.STATUS_SEM_VALOR);
            DocumentReference docRef = db.collection(Constante.USUARIOS).document(usuarioID)
                    .collection(Constante.ORDENS_SERVICOS).document();
            Query query = db.collection(Constante.USUARIOS).document(usuarioID)
                    .collection(Constante.ORDENS_SERVICOS)
                    .whereEqualTo(Constante.NOME_SERVICO, ordemServico.getNomeServico().toLowerCase());
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document :
                            Objects.requireNonNull(task.getResult())) {
                        nomeServicoID = Objects.requireNonNull(document.getString(
                                Constante.NOME_SERVICO)).trim();
                    }
                    if (Objects.equals(ordemServico.getNomeServico().toLowerCase(), nomeServicoID)) {
                        Log.d(Constante.TAG_ORCAMENTO, Constante.NOME_SERVICO_DOIS_PONTOS + nomeServicoID);
                        Toast.makeText(TelaSolicitarOrcamento.this, Constante.SERVICO_JA_EXISTE,
                                Toast.LENGTH_LONG).show();
                    } else {
                        docRef.set(data).addOnSuccessListener(unused -> {
                            Log.d(Constante.TAG_ORCAMENTO, Constante.ID_GERADO + docRef.getId());
                            binding.pBCarregar.setVisibility(View.VISIBLE);
                            binding.btnCadastrar.setEnabled(false);
                            new Handler().postDelayed(() -> {
                                Toast.makeText(TelaSolicitarOrcamento.this,
                                        Constante.ORCAMENTO_ENVIADO, Toast.LENGTH_LONG).show();
                                Objects.requireNonNull(binding.txtNomeServico.getText()).clear();
                                Objects.requireNonNull(binding.txtDescricao.getText()).clear();
                                binding.txtNomeServico.clearFocus();
                                binding.txtDescricao.clearFocus();
                                binding.pBCarregar.setVisibility(View.INVISIBLE);
                                binding.btnCadastrar.setEnabled(true);
                            }, Constante.TEMPO_2SEG);
                        })
                                .addOnFailureListener(e -> {
                                    Log.w(Constante.TAG_ORCAMENTO, Constante.ERRO_ADD_DOCUMENTO, e);
                                    Toast.makeText(TelaSolicitarOrcamento.this,
                                            Constante.ERRO_ENVIAR_ORCAMENTO,
                                            Toast.LENGTH_LONG).show();
                                });
                    }
                } else {
                    Log.d(Constante.TAG_ORCAMENTO, Constante.ERRO_OBTER_DOCUMENTO, task.getException());
                }
            });
        }
    }
}
