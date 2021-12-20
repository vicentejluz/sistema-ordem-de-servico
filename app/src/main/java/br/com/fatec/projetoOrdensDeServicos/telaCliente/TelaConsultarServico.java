package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaChat;
import br.com.fatec.projetoOrdensDeServicos.adapter.OrdemServicoAdapter;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;

public class TelaConsultarServico extends AppCompatActivity {
    private FirebaseFirestore db;
    String usuarioID, statusServico;
    private RecyclerView rVConsultarServico;
    private ArrayList<OrdemServico> ordemServicos;
    private ArrayList<Double> getPrecos;
    private ArrayList<String> getStatus;
    private OrdemServicoAdapter ordemServicoAdapter;
    private ProgressDialog progressDialog;
    SearchView searchView;
    Double preco = 0.0;
    private TextView txtTotalPreco, tVTotalPreco;
    private final Locale LOCALE = new Locale("pt", "BR");
    BottomNavigationView bNVStatus;
    private Boolean isProgressDialog = true;
    private OrdemServicoAdapter.OrdemServicoClickListener ordemServicoClickListener;
    private OrdemServicoAdapter.ChatServicoClickListener chatServicoClickListener;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar);
        bNVStatus = findViewById(R.id.bNVStatus);
        tVTotalPreco = findViewById(R.id.tVTotalPreco);
        txtTotalPreco = findViewById(R.id.txtTotalPreco);
        txtTotalPreco.setText(NumberFormat.getCurrencyInstance(LOCALE).format(preco));
        bNVStatus.getMenu().clear();
        bNVStatus.inflateMenu(R.menu.menu_item_bottom_servico);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Buscando Dados...");
        progressDialog.show();
        rVConsultarServico = findViewById(R.id.rVConsultar);
        rVConsultarServico.setHasFixedSize(true);
        rVConsultarServico.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        ordemServicos = new ArrayList<>();
        getPrecos = new ArrayList<>();
        getStatus = new ArrayList<>();
        setOnClickListener();
        chatServico();

        bNVStatus.setOnItemSelectedListener(item -> {
            limparListas();
            setarVisibilidadeTextView(View.VISIBLE);
            switch (item.getItemId()) {
                case R.id.itmTodos:
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
                    listarMundancaEventoStatus(StatusOrdemServico.FINALIZADA.name());
                    break;
            }
            return true;
        });
    }

    private void limparListas() {
        ordemServicos.clear();
        getPrecos.clear();
        getStatus.clear();
    }

    private void setarVisibilidadeTextView(int visibilidade) {
        tVTotalPreco.setVisibility(visibilidade);
        txtTotalPreco.setVisibility(visibilidade);
    }

    private void setOnClickListener() {
        ordemServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaInformacaoServico.class);
            intent.putExtra("nomeServico", ordemServicos.get(position).getNomeServico());
            startActivity(intent);
            searchView.setQuery("", false);
            searchView.setIconified(true);
        };
    }

    private void chatServico() {
        chatServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaChat.class);
            intent.putExtra("nomeServico", ordemServicos.get(position).getNomeServico());
            intent.putExtra("usuarioID", usuarioID);
            startActivity(intent);
            searchView.setQuery("", false);
            searchView.setIconified(true);
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEvento() {
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance()
                .getCurrentUser()).getUid();
        db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos").orderBy("nomeServico",
                Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        assert progressDialog != null;
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Log.e("Erro no Firestore", error.getMessage());
                        return;
                    }

                    assert value != null;
                    if (progressDialog.isShowing() && value.isEmpty()) {
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(this, TelaMenuCliente.class);
                            startActivity(intent);
                            Toast.makeText(this, "Serviços não encontrado",
                                    Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                        }, 3000);

                    } else {
                        statusServico = null;
                        if (isProgressDialog) {
                            new Handler().postDelayed(() -> procuraServico(value), 2000);
                            isProgressDialog = false;
                        } else
                            procuraServico(value);
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEventoStatus(String status) {
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance()
                .getCurrentUser()).getUid();
        db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos").whereEqualTo("status", status)
                .orderBy("nomeServico", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Erro no Firestore", error.getMessage());
                    } else {
                        assert value != null;
                        statusServico = status;
                        procuraServico(value);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem menuItem = menu.findItem(R.id.sVBuscar);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Buscar");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ordemServicoAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void procuraServico(@NonNull QuerySnapshot value) {
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
                getStatus.clear();
            }
        }
        ordemServicoAdapter = new OrdemServicoAdapter(
                this, ordemServicos, ordemServicoClickListener, chatServicoClickListener);
        rVConsultarServico.setAdapter(ordemServicoAdapter);
        ordemServicoAdapter.notifyDataSetChanged();

        for (Double p : getPrecos)
            preco += p;
        if (preco != null) {
            txtTotalPreco.setText(NumberFormat.getCurrencyInstance(LOCALE).format(preco));
            preco = 0.0;
        }
        progressDialog.dismiss();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bNVStatus.setSelectedItemId(R.id.itmTodos);
    }
}