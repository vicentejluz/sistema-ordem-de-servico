package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.MascaraMonetaria;

public class TelaInformacaoServicoAdmin extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private String nomeServicoCons, nomeServico, descricao, dataFinalizacao;
    private String usuarioID, status, dataAbertura, item, idServico;
    private Timestamp timestampDataAbertura, timestampDataFinalizacao;
    private final Locale LOCALE = new Locale("pt", "BR");
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy",
            LOCALE);
    private TextInputEditText txtNomeServico, txtDescricao, txtPreco, txtDataAbertura;
    private Spinner txtStatus;
    private TextInputEditText txtDataFinalizacao;
    private FirebaseFirestore db;
    private Double preco;
    private Map<String, Object> data;
    private OrdemServico ordemServico;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    String[] statusOrdensServicos = new String[]{
            "AGUARDANDO APROVAÇÃO",
            StatusOrdemServico.ABERTA.name(),
            StatusOrdemServico.CANCELADA.name(),
            StatusOrdemServico.FINALIZADA.name()
    };
    private final List<String> statusServicos = new ArrayList<>(Arrays.asList(statusOrdensServicos));
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacao_servico_admin);
        txtNomeServico = findViewById(R.id.txtNomeServico);
        txtDescricao = findViewById(R.id.txtDescricao);
        txtDataAbertura = findViewById(R.id.txtDataAbertura);
        txtPreco = findViewById(R.id.txtPreco);
        txtDataFinalizacao = findViewById(R.id.txtDataFinal);
        txtStatus = findViewById(R.id.txtStatus);
        Button btnAtualizar = findViewById(R.id.btnAtualizar);
        btnAtualizar.setOnClickListener(this);
        txtStatus.setOnItemSelectedListener(this);
        txtPreco.addTextChangedListener(new MascaraMonetaria(txtPreco, LOCALE));
        db = FirebaseFirestore.getInstance();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
                statusServicos) {
            @Override
            public boolean isEnabled(int position) {
                if (status.equals(StatusOrdemServico.CANCELADA
                        .toString())) {
                    if (position == 3)
                        return false;
                    if (position == 0)
                        return true;
                }
                if (timestampDataAbertura == null || status.equals("AGUARDANDO APROVAÇÃO")) {
                    if (position == 1)
                        return false;
                    if (position == 3)
                        return false;
                }

                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (timestampDataAbertura == null || status.equals("AGUARDANDO APROVAÇÃO")) {
                    if (position == 1)
                        tv.setTextColor(Color.GRAY);
                    if (position == 3)
                        tv.setTextColor(Color.GRAY);
                }
                if (status.equals(StatusOrdemServico.CANCELADA
                        .name())) {
                    if (position == 3)
                        tv.setTextColor(Color.GRAY);
                } else if (status.equals(StatusOrdemServico.ABERTA
                        .name())) {
                    if (position == 0)
                        tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        txtStatus.setAdapter(arrayAdapter);
        data = new HashMap<>();
        dadosItemLista();
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregarOrdemServico();
    }

    public void onClick(@NonNull View v) {
        atualizarServico(item);
    }

    private void carregarOrdemServico() {
        Query query = db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", nomeServicoCons);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            idServico = document.getId();
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

    @SuppressLint("SetTextI18n")
    private void setarDadosServico() {
        ordemServico = new OrdemServico(nomeServico, descricao, preco,
                timestampDataAbertura, timestampDataFinalizacao, status);

        if (ordemServico.getDataAbertura() != null)
            dataAbertura = SIMPLE_DATE_FORMAT.format(ordemServico.getDataAbertura().toDate());
        if (ordemServico.getDatafinalizacao() != null)
            dataFinalizacao = SIMPLE_DATE_FORMAT.format(ordemServico.getDatafinalizacao().toDate());

        txtNomeServico.setText(ordemServico.getNomeServico().substring(0, 1).toUpperCase()
                .concat(ordemServico.getNomeServico().substring(1)));
        txtDescricao.setText(ordemServico.getDescricao().substring(0, 1).toUpperCase()
                .concat(ordemServico.getDescricao().substring(1)));
        txtPreco.setText(decimalFormat.format(ordemServico.getPreco()));

        txtDataAbertura.setText(dataAbertura);
        txtDataFinalizacao.setText(dataFinalizacao);
        if (status.equals("-"))
            txtStatus.setSelection(0);
        else if (status.equals(StatusOrdemServico.ABERTA.name()))
            txtStatus.setSelection(1);
        else if (status.equals(StatusOrdemServico.CANCELADA.name()))
            txtStatus.setSelection(2);
        else
            txtStatus.setSelection(3);
    }

    private void dadosItemLista() {
        nomeServicoCons = "Nome do serviço não foi setado";
        usuarioID = "ID do usuário não foi setado";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeServicoCons = extras.getString("nomeServico");
            usuarioID = extras.getString("usuarioID");
        }
    }

    private void status() {
        if (txtStatus.getSelectedItem().toString().equals("AGUARDANDO APROVAÇÃO")) {
            txtStatus.setEnabled(false);
        } else {
            if (txtStatus.getSelectedItem().toString().equals(StatusOrdemServico.ABERTA.name())) {
                txtPreco.setFocusable(false);
                txtPreco.setCursorVisible(false);
            } else {
                if (txtStatus.getSelectedItem().toString().equals(StatusOrdemServico.FINALIZADA
                        .name())) {
                    txtStatus.setEnabled(false);
                    txtDescricao.setFocusable(false);
                    txtDescricao.setCursorVisible(false);
                    txtPreco.setFocusable(false);
                    txtPreco.setCursorVisible(false);
                } else {
                    if (txtStatus.getSelectedItem().toString().equals(StatusOrdemServico.CANCELADA
                            .name())) {
                        txtDescricao.setFocusable(false);
                        txtDescricao.setCursorVisible(false);
                        txtPreco.setFocusableInTouchMode(true);
                        txtPreco.setCursorVisible(true);
                    }
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        item = txtStatus.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void atualizarServico(@NonNull String item) {
        Timestamp timestamp = Timestamp.now();
        String conStringPreco = Objects.requireNonNull(txtPreco.getText()).toString()
                .replaceAll("[^0-9,]", "").replace(",", ".");
        ordemServico.setPreco(Double.valueOf(conStringPreco));
        if ((item.equals(StatusOrdemServico.CANCELADA.name())) &&
                (!preco.equals(ordemServico.getPreco()))) {
            Toast.makeText(this, "Para Mudar preço depois de cancelado mude status" +
                    " para, aguardando aprovação!!", Toast.LENGTH_LONG).show();
            onStart();
        } else if ((item.equals(StatusOrdemServico.ABERTA.name())) &&
                (!preco.equals(ordemServico.getPreco()))) {
            Toast.makeText(this, "Para Mudar preço depois de Aberto o cliente tem que " +
                    "cancelar o serviço", Toast.LENGTH_LONG).show();
            onStart();
        } else if (item.equals("AGUARDANDO APROVAÇÃO") && ordemServico.getPreco() == 0.0) {
            Toast.makeText(this, "Adicione um valor maior que 0 para preço",
                    Toast.LENGTH_LONG).show();
        } else {
            if (item.equals("AGUARDANDO APROVAÇÃO"))
                item = "-";
            if (!status.equals(item))
                data.put("status", item);
            ordemServico.setDescricao(Objects.requireNonNull(txtDescricao.getText()).toString().trim());
            if (!descricao.equals(ordemServico.getDescricao()))
                data.put("descricao", ordemServico.getDescricao());
            if (!preco.equals(ordemServico.getPreco()))
                data.put("preco", ordemServico.getPreco());
            if (item.equals(StatusOrdemServico.FINALIZADA.name()))
                data.put("dataFinalizacao", timestamp);
            DocumentReference docRef = db.collection("usuarios").document(usuarioID)
                    .collection("ordensDeServicos").document(idServico);
            docRef.update(data);
            Toast.makeText(this, "Atualizado com sucesso!!",
                    Toast.LENGTH_LONG).show();
            txtPreco.clearFocus();
            txtDescricao.clearFocus();
            onStart();
        }
    }
}