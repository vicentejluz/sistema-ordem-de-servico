package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaChat;
import br.com.fatec.projetoOrdensDeServicos.adapter.OrdemServicoAdapter;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityConsultarBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaConsultarServico extends AppCompatActivity {
    private FirebaseFirestore db;
    private String usuarioID, statusServico;
    private ArrayList<OrdemServico> ordemServicos;
    private ActivityConsultarBinding binding;
    private ArrayList<Double> getPrecos;
    private ArrayList<String> getStatus;
    private OrdemServicoAdapter ordemServicoAdapter;
    private ProgressDialog progressDialog;
    private SearchView searchView;
    private Double preco = 0.0;
    private Boolean isProgressDialog = true;
    private OrdemServicoAdapter.OrdemServicoClickListener ordemServicoClickListener;
    private OrdemServicoAdapter.ChatServicoClickListener chatServicoClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsultarBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.txtTotalPreco.setText(NumberFormat.getCurrencyInstance(Constante.LOCALE).format(preco));
        binding.bNVStatus.getMenu().clear();
        binding.bNVStatus.inflateMenu(R.menu.menu_item_bottom_servico);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(Constante.BUSCANDO_DADOS);
        progressDialog.show();
        binding.rVConsultar.setHasFixedSize(true);
        binding.rVConsultar.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        ordemServicos = new ArrayList<>();
        getPrecos = new ArrayList<>();
        getStatus = new ArrayList<>();
        setOnClickListener();
        chatServico();

        binding.bNVStatus.setOnItemSelectedListener(item -> {
            limparListas();
            setarVisibilidadeTextView(View.VISIBLE);
            if (item.getItemId() == R.id.itmTodos)
                listarMundancaEvento();
            else if (item.getItemId() == R.id.itmAberta)
                listarMundancaEventoStatus(StatusOrdemServico.ABERTA.name());
            else if (item.getItemId() == R.id.itmCancelada) {
                setarVisibilidadeTextView(View.GONE);
                listarMundancaEventoStatus(StatusOrdemServico.CANCELADA.name());
            } else
                listarMundancaEventoStatus(StatusOrdemServico.FINALIZADA.name());
            return true;
        });
    }

    private void limparListas() {
        ordemServicos.clear();
        getPrecos.clear();
        getStatus.clear();
    }

    private void setarVisibilidadeTextView(int visibilidade) {
        binding.tVTotalPreco.setVisibility(visibilidade);
        binding.txtTotalPreco.setVisibility(visibilidade);
    }

    private void setOnClickListener() {
        ordemServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaInformacaoServico.class);
            intent.putExtra(Constante.NOME_SERVICO, ordemServicos.get(position).getNomeServico());
            startActivity(intent);
            searchView.setQuery("", false);
            searchView.setIconified(true);
        };
    }

    private void chatServico() {
        chatServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaChat.class);
            intent.putExtra(Constante.NOME_SERVICO, ordemServicos.get(position).getNomeServico());
            intent.putExtra(Constante.USUARIO_ID, usuarioID);
            startActivity(intent);
            searchView.setQuery("", false);
            searchView.setIconified(true);
        };
    }

    private void listarMundancaEvento() {
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance()
                .getCurrentUser()).getUid();
        db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS).orderBy(Constante.NOME_SERVICO,
                Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                        return;
                    }
                    if (value != null) {
                        if (progressDialog.isShowing() && value.isEmpty()) {
                            new Handler().postDelayed(() -> {
                                Intent intent = new Intent(this, TelaMenuCliente.class);
                                startActivity(intent);
                                Toast.makeText(this, Constante.SERVICO_NAO_ENCONTRADO,
                                        Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();

                            }, Constante.TEMPO_3SEG);

                        } else {
                            statusServico = null;
                            if (isProgressDialog) {
                                new Handler().postDelayed(() -> procuraServico(value),
                                        Constante.TEMPO_2SEG);
                                isProgressDialog = false;
                            } else
                                procuraServico(value);
                        }
                    }
                });
    }

    private void listarMundancaEventoStatus(String status) {
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance()
                .getCurrentUser()).getUid();
        db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS).whereEqualTo(Constante.STATUS, status)
                .orderBy(Constante.NOME_SERVICO, Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                    } else {
                        statusServico = status;
                        procuraServico(Objects.requireNonNull(value));
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem menuItem = menu.findItem(R.id.sVBuscar);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(Constante.BUSCAR);
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
                getStatus.clear();
            }
        }
        ordemServicoAdapter = new OrdemServicoAdapter(
                this, ordemServicos, ordemServicoClickListener, chatServicoClickListener);
        binding.rVConsultar.setAdapter(ordemServicoAdapter);
        ordemServicoAdapter.notifyDataSetChanged();

        for (Double p : getPrecos)
            preco += p;
        if (preco != null) {
            binding.txtTotalPreco.setText(NumberFormat.getCurrencyInstance(Constante.LOCALE)
                    .format(preco));
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
        binding.bNVStatus.setSelectedItemId(R.id.itmTodos);
    }
}