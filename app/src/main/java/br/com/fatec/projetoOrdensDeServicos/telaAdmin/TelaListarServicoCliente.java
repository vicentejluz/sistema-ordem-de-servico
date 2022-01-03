package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class TelaListarServicoCliente extends AppCompatActivity {
    private ActivityConsultarBinding binding;
    private FirebaseFirestore db;
    private ArrayList<OrdemServico> ordemServicos;
    private OrdemServicoAdminAdapter ordemServicoAdminAdapter;
    private String usuarioID, cliente, statusServico;
    private ProgressDialog progressDialog;
    private Boolean isProgressDialog = true;
    private Double preco = 0.0, getPreco = 0.0;
    private final Locale LOCALE = new Locale("pt", "BR");
    private ArrayList<Double> getPrecos;
    private ArrayList<String> getStatus;
    private SearchView searchView;
    private String ordemServicoID;
    private boolean isStatus;
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
        binding.txtTotalPreco.setText(NumberFormat.getCurrencyInstance(LOCALE).format(preco));
        binding.bNVStatus.getMenu().clear();
        binding.bNVStatus.inflateMenu(R.menu.menu_item_bottom_servico);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Buscando Dados...");
        progressDialog.show();
        binding.rVConsultar.setHasFixedSize(true);
        binding.rVConsultar.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        ordemServicos = new ArrayList<>();
        getPrecos = new ArrayList<>();
        getStatus = new ArrayList<>();
        dadosItemLista();
        setOnClickListener();
        confirmarExcluirOrderServico();
        chatServico();
        Objects.requireNonNull(getSupportActionBar()).setTitle(cliente.substring(0, 1).toUpperCase()
                .concat(cliente.substring(1).toLowerCase()));
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

    private void limparListas() {
        ordemServicos.clear();
        getPrecos.clear();
        getStatus.clear();
    }

    private void setarVisibilidadeTextView(int visibilidade) {
        binding.tVTotalPreco.setVisibility(visibilidade);
        binding.txtTotalPreco.setVisibility(visibilidade);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bNVStatus.setSelectedItemId(R.id.itmTodos);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void chatServico() {
        chatServicoClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaChat.class);
            intent.putExtra("nomeServico", ordemServicos.get(position).getNomeServico());
            intent.putExtra("cliente", cliente);
            intent.putExtra("usuarioID", usuarioID);
            startActivity(intent);
            searchView.setQuery("", false);
            searchView.setIconified(true);
        };
    }

    private void dadosItemLista() {
        usuarioID = "ID do usuário não foi setado";
        cliente = "Cliente não setado";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cliente = extras.getString("nomeCliente");
            usuarioID = extras.getString("usuarioID");
        }
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

    private void setOnClickListener() {
        ordemServicoAdminClickListener = (v, position) -> {
            Intent intent = new Intent(this, TelaInformacaoServicoAdmin.class);
            intent.putExtra("nomeServico", ordemServicos.get(position).getNomeServico());
            intent.putExtra("usuarioID", usuarioID);
            startActivity(intent);
            searchView.setQuery("", false);
            searchView.setIconified(true);
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listarMundancaEvento() {
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
                            Toast.makeText(this, "Nenhum serviço cadastrado!!",
                                    Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, TelaListarCliente.class);
                            startActivity(intent);
                            finish();
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
            ordemServicoAdminAdapter = new OrdemServicoAdminAdapter(
                    this, ordemServicos, new ArrayList<>(), ordemServicoAdminClickListener,
                    excluirOrdemServicoClickListener, chatServicoClickListener);
            binding.rVConsultar.setAdapter(ordemServicoAdminAdapter);
            ordemServicoAdminAdapter.notifyDataSetChanged();
        }
        for (Double p : getPrecos)
            preco += p;
        if (preco != null) {
            getPreco = preco;
            binding.txtTotalPreco.setText(NumberFormat.getCurrencyInstance(LOCALE).format(getPreco));
            preco = 0.0;
        }
        progressDialog.dismiss();
    }

    private void excluirOrderServico(int position) {
        db.collection("usuarios").document(usuarioID)
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
                        ExcluirComentario();
                        db.collection("usuarios").document(usuarioID)
                                .collection("ordensDeServicos").document(ordemServicoID)
                                .delete();
                        Toast.makeText(this, "Serviço deletado com sucesso",
                                Toast.LENGTH_LONG).show();
                        getPreco = getPreco - ordemServicos.get(position).getPreco();
                        /*Depois ver como limpar lista sem duplicar*/
                        if (!isStatus)
                            binding.bNVStatus.setSelectedItemId(R.id.itmFinalizada);
                        else
                            binding.bNVStatus.setSelectedItemId(R.id.itmTodos);
                    } else {
                        Log.d("ERROR:", "Erro ao achar documento: ",
                                task.getException());
                    }
                });
    }

    private void ExcluirComentario() {
        db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .document(ordemServicoID)
                .collection("comentarios")
                .document(usuarioID)
                .collection(IDCHAT).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documento :
                                Objects.requireNonNull(task.getResult())) {
                            db.collection("usuarios").document(usuarioID)
                                    .collection("ordensDeServicos")
                                    .document(ordemServicoID)
                                    .collection("comentarios")
                                    .document(usuarioID)
                                    .collection(IDCHAT)
                                    .document(documento.getId()).delete();
                        }
                    }
                });
        db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .document(ordemServicoID)
                .collection("comentarios")
                .document(IDCHAT)
                .collection(usuarioID).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documento :
                                Objects.requireNonNull(task.getResult())) {
                            db.collection("usuarios").document(usuarioID)
                                    .collection("ordensDeServicos")
                                    .document(ordemServicoID)
                                    .collection("comentarios")
                                    .document(IDCHAT)
                                    .collection(usuarioID)
                                    .document(documento.getId()).delete();
                        }
                    }
                });
    }

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
                ordemServicoAdminAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }
}