package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityInformacaoServicoBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.MascaraMonetaria;

public class TelaInformacaoServico extends AppCompatActivity implements View.OnClickListener {
    private String nomeServicoCons, descricao, nomeServico, dataAbertura, status, dataFinalizacao;
    private Double preco;
    private ActivityInformacaoServicoBinding binding;
    private String usuarioID, idServico;
    private Map<String, Object> data;
    private Timestamp timestampDataAbertura, timestampDataFinalizacao;
    private final Locale LOCALE = new Locale("pt", "BR");
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", LOCALE);
    private FirebaseFirestore db;
    private OrdemServico ordemServico;
    private static final String TAG = "Informação do servico";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformacaoServicoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnAceitar.setOnClickListener(this);
        binding.btnCancelar.setOnClickListener(this);
        db = FirebaseFirestore.getInstance();
        data = new HashMap<>();
        dadosItemLista();
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregarOrdemServico();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnAceitar:
                aceitarServico();
                break;
            case R.id.btnCancelar:
                cancelarServico();
                break;
        }
    }

    private void carregarOrdemServico() {
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                .getUid();
        Query query = db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", nomeServicoCons);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            nomeServico = Objects.requireNonNull(document
                                    .getString("nomeServico"));
                            descricao = Objects.requireNonNull(document
                                    .getString("descricao"));
                            timestampDataAbertura = document.getTimestamp("dataAbertura");
                            status = document.getString("status");
                            preco = document.getDouble("preco");
                            timestampDataFinalizacao = document.getTimestamp("dataFinalizacao");
                        }
                        setarDadosServico();
                        status();
                    } else {
                        Toast.makeText(this, "Serviços não encontrado",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void dadosItemLista() {
        nomeServicoCons = "Nome do serviço não foi setado";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeServicoCons = extras.getString("nomeServico");
        }
    }


    private void status() {
        if (Objects.requireNonNull(binding.txtStatus.getText()).toString().equals(
                StatusOrdemServico.CANCELADA.name())) {
            binding.btnCancelar.setEnabled(false);
            binding.btnAceitar.setEnabled(false);
        } else {
            if (Objects.requireNonNull(binding.txtStatus.getText()).toString().equals(
                    StatusOrdemServico.ABERTA.name())) {
                binding.btnAceitar.setEnabled(false);
                binding.btnCancelar.setEnabled(true);
            } else {
                if ((Objects.requireNonNull(binding.txtStatus.getText()).toString().equals(
                        StatusOrdemServico.FINALIZADA.name()))) {
                    binding.btnAceitar.setEnabled(false);
                    binding.btnCancelar.setEnabled(false);
                } else {
                    if (preco > 0)
                        binding.btnAceitar.setEnabled(true);
                    binding.btnCancelar.setEnabled(true);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setarDadosServico() {
        ordemServico = new OrdemServico(nomeServico, descricao, preco,
                timestampDataAbertura, timestampDataFinalizacao, status);
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (ordemServico.getDataAbertura() != null)
            dataAbertura = SIMPLE_DATE_FORMAT.format(ordemServico.getDataAbertura().toDate());
        if (ordemServico.getDatafinalizacao() != null)
            dataFinalizacao = SIMPLE_DATE_FORMAT.format(ordemServico.getDatafinalizacao().toDate());

        binding.txtNomeServico.setText(ordemServico.getNomeServico().substring(0, 1).toUpperCase()
                .concat(ordemServico.getNomeServico().substring(1)));
        binding.txtDescricao.setText(ordemServico.getDescricao().substring(0, 1).toUpperCase()
                .concat(ordemServico.getDescricao().substring(1)));
        if (preco <= 0)
            binding.txtPreco.setText("");
        else {
            binding.txtPreco.addTextChangedListener(new MascaraMonetaria(binding.txtPreco, LOCALE));
            binding.txtPreco.setText(decimalFormat.format(ordemServico.getPreco()));
        }
        binding.txtDataAbertura.setText(dataAbertura);
        binding.txtDataFinal.setText(dataFinalizacao);
        if (status.equals("-")) {
            binding.txtStatus.setText("");
        } else {
            binding.txtStatus.setText(ordemServico.getStatus());
        }
    }

    private void aceitarServico() {
        binding.btnAceitar.setEnabled(false);
        Timestamp timestamp = Timestamp.now();
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                .getUid();
        data.put("status", StatusOrdemServico.ABERTA.name());
        if (ordemServico.getDataAbertura() == null)
            data.put("dataAbertura", timestamp);
        Query queryCancel = db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", nomeServicoCons);
        queryCancel.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document :
                        Objects.requireNonNull(task.getResult())) {
                    idServico = document.getId();
                }
                DocumentReference docRef = db.collection("usuarios")
                        .document(usuarioID).collection("ordensDeServicos")
                        .document(idServico);
                docRef.update(data);
                onStart();
            } else {
                Log.d(TAG, "Erro ao consultar documento: ", task.getException());
            }
        });
    }

    private void cancelarServico() {
        binding.btnCancelar.setEnabled(false);
        binding.btnAceitar.setEnabled(false);
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                .getUid();
        data.put("status", StatusOrdemServico.CANCELADA.name());
        Query queryCancel = db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", nomeServicoCons);
        queryCancel.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document :
                        Objects.requireNonNull(task.getResult())) {
                    idServico = document.getId();
                }
                DocumentReference docRef = db.collection("usuarios")
                        .document(usuarioID).collection("ordensDeServicos")
                        .document(idServico);
                docRef.update(data);
                onStart();
            } else {
                Log.d(TAG, "Erro ao consultar documento: ", task.getException());
            }
        });
    }
}
