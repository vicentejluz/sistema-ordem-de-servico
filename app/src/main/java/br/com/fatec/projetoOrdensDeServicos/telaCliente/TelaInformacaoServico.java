package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Map;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityInformacaoServicoBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;
import br.com.fatec.projetoOrdensDeServicos.util.MascaraMonetaria;

public class TelaInformacaoServico extends AppCompatActivity {
    private String nomeServicoCons, descricao, nomeServico, dataAbertura, status, dataFinalizacao;
    private Double preco;
    private ActivityInformacaoServicoBinding binding;
    private String usuarioID, idServico;
    private Map<String, Object> data;
    private Timestamp timestampDataAbertura, timestampDataFinalizacao;
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            Constante.MASCARA_DATA_BR, Constante.LOCALE);
    private FirebaseFirestore db;
    private OrdemServico ordemServico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformacaoServicoBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnAceitar.setOnClickListener(v -> aceitarServico());
        binding.btnCancelar.setOnClickListener(v -> cancelarServico());
        db = FirebaseFirestore.getInstance();
        data = new HashMap<>();
        dadosItemLista();
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregarOrdemServico();
    }

    private void carregarOrdemServico() {
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                .getUid();
        Query query = db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, nomeServicoCons);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            nomeServico = Objects.requireNonNull(document
                                    .getString(Constante.NOME_SERVICO));
                            descricao = Objects.requireNonNull(document
                                    .getString(Constante.DESCRICAO));
                            timestampDataAbertura = document.getTimestamp(Constante.DATA_ABERTURA);
                            status = document.getString(Constante.STATUS);
                            preco = document.getDouble(Constante.PRECO);
                            timestampDataFinalizacao = document.getTimestamp(
                                    Constante.DATA_FINALIZACAO);
                        }
                        setarDadosServico();
                        status();
                    } else {
                        Toast.makeText(this, Constante.SERVICO_NAO_ENCONTRADO,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void dadosItemLista() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeServicoCons = extras.getString(Constante.NOME_SERVICO);
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

    private void setarDadosServico() {
        ordemServico = new OrdemServico(nomeServico, descricao, preco,
                timestampDataAbertura, timestampDataFinalizacao, status);
        DecimalFormat decimalFormat = new DecimalFormat(Constante.ZERO);

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
            binding.txtPreco.addTextChangedListener(new MascaraMonetaria(binding.txtPreco,
                    Constante.LOCALE));
            binding.txtPreco.setText(decimalFormat.format(ordemServico.getPreco()));
        }
        binding.txtDataAbertura.setText(dataAbertura);
        binding.txtDataFinal.setText(dataFinalizacao);
        if (status.equals(Constante.STATUS_SEM_VALOR)) {
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
        data.put(Constante.STATUS, StatusOrdemServico.ABERTA.name());
        if (ordemServico.getDataAbertura() == null)
            data.put(Constante.DATA_ABERTURA, timestamp);
        Query queryCancel = db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, nomeServicoCons);
        queryCancel.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document :
                        Objects.requireNonNull(task.getResult())) {
                    idServico = document.getId();
                }
                DocumentReference docRef = db.collection(Constante.USUARIOS)
                        .document(usuarioID).collection(Constante.ORDENS_SERVICOS)
                        .document(idServico);
                docRef.update(data);
                onStart();
            } else {
                Log.d(Constante.TAG_INFORMACAO_SERVICO, Constante.ERRO_OBTER_DOCUMENTO,
                        task.getException());
            }
        });
    }

    private void cancelarServico() {
        binding.btnCancelar.setEnabled(false);
        binding.btnAceitar.setEnabled(false);
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                .getUid();
        data.put(Constante.STATUS, StatusOrdemServico.CANCELADA.name());
        Query queryCancel = db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, nomeServicoCons);
        queryCancel.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document :
                        Objects.requireNonNull(task.getResult())) {
                    idServico = document.getId();
                }
                DocumentReference docRef = db.collection(Constante.USUARIOS)
                        .document(usuarioID).collection(Constante.ORDENS_SERVICOS)
                        .document(idServico);
                docRef.update(data);
                onStart();
            } else {
                Log.d(Constante.TAG_INFORMACAO_SERVICO, Constante.ERRO_OBTER_DOCUMENTO,
                        task.getException());
            }
        });
    }
}
