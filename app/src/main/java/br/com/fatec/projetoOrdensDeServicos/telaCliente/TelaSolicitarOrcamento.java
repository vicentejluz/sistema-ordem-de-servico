package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.os.Build;
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

@RequiresApi(api = Build.VERSION_CODES.O)
public class TelaSolicitarOrcamento extends AppCompatActivity implements View.OnClickListener {
    private String nomeServicoID;
    private FirebaseFirestore db;
    private Map<String, Object> data;
    private OrdemServico ordemServico;
    private ActivitySolicitarOrcamentoBinding binding;
    private static final String TAG = "Orçamento";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivitySolicitarOrcamentoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = FirebaseFirestore.getInstance();
        data = new HashMap<>();
        TooltipCompat.setTooltipText(binding.imBVoltar, "Voltar");
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
        final String STATUS = "-";
        final Double PRECO = 0.0;
        ordemServico = new OrdemServico(nomeServico, descricao, PRECO);
        if (ordemServico.getNomeServico().isEmpty() || ordemServico.getDescricao().isEmpty()) {
            Toast.makeText(this, "ERRO - Preencha todos os campos",
                    Toast.LENGTH_LONG).show();
        } else {
            String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                    .getUid();
            data.put("nomeServico", ordemServico.getNomeServico().toLowerCase());
            data.put("descricao", ordemServico.getDescricao().toLowerCase());
            data.put("preco", ordemServico.getPreco());
            data.put("dataAbertura", ordemServico.getDataAbertura());
            data.put("dataFinalizacao", ordemServico.getDatafinalizacao());
            data.put("status", STATUS);
            DocumentReference docRef = db.collection("usuarios").document(usuarioID)
                    .collection("ordensDeServicos").document();
            Query query = db.collection("usuarios").document(usuarioID)
                    .collection("ordensDeServicos")
                    .whereEqualTo("nomeServico", ordemServico.getNomeServico().toLowerCase());
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document :
                            Objects.requireNonNull(task.getResult())) {
                        nomeServicoID = Objects.requireNonNull(document.getString("nomeServico"))
                                .trim();
                    }
                    if (Objects.equals(ordemServico.getNomeServico().toLowerCase(), nomeServicoID)) {
                        Log.d(TAG, "Nome do Serviço: " + nomeServicoID);
                        Toast.makeText(TelaSolicitarOrcamento.this, "Serviço já existe",
                                Toast.LENGTH_LONG).show();
                    } else {
                        docRef.set(data).addOnSuccessListener(unused -> {
                            Log.d(TAG, "ID gerado: " + docRef.getId());
                            binding.pBCarregar.setVisibility(View.VISIBLE);
                            binding.btnCadastrar.setEnabled(false);
                            new Handler().postDelayed(() -> {
                                Toast.makeText(TelaSolicitarOrcamento.this,
                                        "Orçamento enviado.", Toast.LENGTH_LONG).show();
                                Objects.requireNonNull(binding.txtNomeServico.getText()).clear();
                                Objects.requireNonNull(binding.txtDescricao.getText()).clear();
                                binding.txtNomeServico.clearFocus();
                                binding.txtDescricao.clearFocus();
                                binding.pBCarregar.setVisibility(View.INVISIBLE);
                                binding.btnCadastrar.setEnabled(true);
                            }, 2000);
                        })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Erro ao adicionar o documento", e);
                                    Toast.makeText(TelaSolicitarOrcamento.this,
                                            "Erro ao enviar orçamento!",
                                            Toast.LENGTH_LONG).show();
                                });
                    }
                } else {
                    Log.d(TAG, "Erro ao consultar documento: ", task.getException());
                }
            });
        }
    }
}
