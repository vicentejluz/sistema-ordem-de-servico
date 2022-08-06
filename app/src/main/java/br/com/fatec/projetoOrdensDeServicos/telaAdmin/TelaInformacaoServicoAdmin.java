package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityInformacaoServicoAdminBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;
import br.com.fatec.projetoOrdensDeServicos.util.MascaraMonetaria;

public class TelaInformacaoServicoAdmin extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {
    private String nomeServicoCons, nomeServico, descricao, dataFinalizacao;
    private String usuarioID, status, dataAbertura, item, idServico;
    private Timestamp timestampDataAbertura, timestampDataFinalizacao;
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(Constante.MASCARA_DATA_BR,
            Constante.LOCALE);
    private ActivityInformacaoServicoAdminBinding binding;
    private FirebaseFirestore db;
    private Double preco;
    private Map<String, Object> data;
    private OrdemServico ordemServico;
    DecimalFormat decimalFormat = new DecimalFormat(Constante.ZERO);
    String[] statusOrdensServicos = new String[]{
            StatusOrdemServico.PROCESSANDO.name(),
            StatusOrdemServico.ABERTA.name(),
            StatusOrdemServico.CANCELADA.name(),
            StatusOrdemServico.FINALIZADA.name()
    };
    private final List<String> statusServicos = new ArrayList<>(Arrays.asList(statusOrdensServicos));
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInformacaoServicoAdminBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnAtualizar.setOnClickListener(this);
        binding.txtStatus.setOnItemSelectedListener(this);
        binding.txtPreco.addTextChangedListener(new MascaraMonetaria(binding.txtPreco, Constante.LOCALE));
        db = FirebaseFirestore.getInstance();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
                statusServicos) {

            @Override
            public boolean isEnabled(int position) {
                if (status.equals(StatusOrdemServico.CANCELADA.name())) {
                    if (position == 3)
                        return false;
                    if (position == 0)
                        return true;
                }
                if (timestampDataAbertura == null) {
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
                if (timestampDataAbertura == null) {
                    if (position == 1)
                        tv.setTextColor(Color.GRAY);
                    if (position == 3)
                        tv.setTextColor(Color.GRAY);
                }
                if (status.equals(StatusOrdemServico.CANCELADA.name())) {
                    if (position == 3)
                        tv.setTextColor(Color.GRAY);
                    if (position == 0)
                        tv.setTextColor(Color.BLACK);
                } else if (status.equals(StatusOrdemServico.ABERTA.name())) {
                    if (position == 0)
                        tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.txtStatus.setAdapter(arrayAdapter);
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
        Query query = db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, nomeServicoCons);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            idServico = document.getId();
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


    private void setarDadosServico() {
        ordemServico = new OrdemServico(nomeServico, descricao, preco,
                timestampDataAbertura, timestampDataFinalizacao, StatusOrdemServico.valueOf(status));

        if (ordemServico.getDataAbertura() != null)
            dataAbertura = SIMPLE_DATE_FORMAT.format(ordemServico.getDataAbertura().toDate());
        if (ordemServico.getDatafinalizacao() != null)
            dataFinalizacao = SIMPLE_DATE_FORMAT.format(ordemServico.getDatafinalizacao().toDate());

        binding.txtNomeServico.setText(ordemServico.getNomeServico().substring(0, 1).toUpperCase()
                .concat(ordemServico.getNomeServico().substring(1)));
        binding.txtDescricao.setText(ordemServico.getDescricao().substring(0, 1).toUpperCase()
                .concat(ordemServico.getDescricao().substring(1)));
        if(ordemServico.getPreco() != null)
            binding.txtPreco.setText(decimalFormat.format(ordemServico.getPreco()));

        binding.txtDataAbertura.setText(dataAbertura);
        binding.txtDataFinal.setText(dataFinalizacao);
        if (status.equals(StatusOrdemServico.PROCESSANDO.name()))
            binding.txtStatus.setSelection(0);
        else if (status.equals(StatusOrdemServico.ABERTA.name()))
            binding.txtStatus.setSelection(1);
        else if (status.equals(StatusOrdemServico.CANCELADA.name()))
            binding.txtStatus.setSelection(2);
        else
            binding.txtStatus.setSelection(3);
    }

    private void dadosItemLista() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeServicoCons = extras.getString(Constante.NOME_SERVICO);
            usuarioID = extras.getString(Constante.USUARIO_ID);
        }
    }

    private void status() {
        if (binding.txtStatus.getSelectedItem().toString().equals(StatusOrdemServico.PROCESSANDO
                .name())) {
            binding.txtStatus.setEnabled(false);
        } else {
            if (binding.txtStatus.getSelectedItem().toString().equals(StatusOrdemServico.ABERTA
                    .name())) {
                binding.txtPreco.setFocusable(false);
                binding.txtPreco.setCursorVisible(false);
            } else {
                if (binding.txtStatus.getSelectedItem().toString().equals(StatusOrdemServico
                        .FINALIZADA.name())) {
                    binding.txtStatus.setEnabled(false);
                    binding.txtDescricao.setFocusable(false);
                    binding.txtDescricao.setCursorVisible(false);
                    binding.txtPreco.setFocusable(false);
                    binding.txtPreco.setCursorVisible(false);
                } else {
                    if (binding.txtStatus.getSelectedItem().toString().equals(StatusOrdemServico
                            .CANCELADA.name())) {
                        binding.txtDescricao.setFocusable(false);
                        binding.txtDescricao.setCursorVisible(false);
                        binding.txtPreco.setFocusableInTouchMode(true);
                        binding.txtPreco.setCursorVisible(true);
                    }
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        item = binding.txtStatus.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void atualizarServico(@NonNull String item) {
        Timestamp timestamp = Timestamp.now();
        String conStringPreco = Objects.requireNonNull(binding.txtPreco.getText()).toString()
                .replaceAll("[^0-9,]", "").replace(",", ".");
        ordemServico.setPreco(Double.valueOf(conStringPreco));
        if ((item.equals(StatusOrdemServico.CANCELADA.name())) &&
                (!ordemServico.getPreco().equals(preco))) {
            Toast.makeText(this, Constante.MUDAR_CANCELADO_PARA_PROCESSANDO,
                    Toast.LENGTH_LONG).show();
            onStart();
        } else if ((item.equals(StatusOrdemServico.ABERTA.name())) &&
                (!ordemServico.getPreco().equals(preco))) {
            Toast.makeText(this, Constante.MUDAR_ABERTO_TEM_CANCELAR, Toast.LENGTH_LONG).show();
            onStart();
        } else if (item.equals(StatusOrdemServico.PROCESSANDO.name()) && ordemServico.getPreco() <= 0.0) {
            Toast.makeText(this, Constante.ADD_VALOR_MAIOR_0, Toast.LENGTH_LONG).show();
        } else {
            if (item.equals(StatusOrdemServico.PROCESSANDO.name()))
                item = StatusOrdemServico.PROCESSANDO.name();
            if (!status.equals(item))
                data.put(Constante.STATUS, item);
            ordemServico.setDescricao(Objects.requireNonNull(binding.txtDescricao.getText())
                    .toString().trim());
            if (!descricao.equals(ordemServico.getDescricao()))
                data.put(Constante.DESCRICAO, ordemServico.getDescricao());
            if (!ordemServico.getPreco().equals(preco))
                data.put(Constante.PRECO, ordemServico.getPreco());
            if (item.equals(StatusOrdemServico.FINALIZADA.name()))
                data.put(Constante.DATA_ABERTURA, timestamp);
            DocumentReference docRef = db.collection(Constante.USUARIOS).document(usuarioID)
                    .collection(Constante.ORDENS_SERVICOS).document(idServico);
            docRef.update(data);
            Toast.makeText(this, Constante.ATUALIZADO_SUCESSO, Toast.LENGTH_LONG).show();
            binding.txtPreco.clearFocus();
            binding.txtDescricao.clearFocus();
            onStart();
        }
    }
}