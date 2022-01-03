package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaLogin;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityMenuClienteBinding;

public class TelaMenuCliente extends AppCompatActivity implements View.OnClickListener {
    private ActivityMenuClienteBinding binding;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuClienteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TooltipCompat.setTooltipText(binding.btnSair, "Deslogar");
        binding.btnSair.setOnClickListener(this);
        binding.btnCliente.setOnClickListener(this);
        binding.btnOrdemServico.setOnClickListener(this);
        binding.btnConsultarServico.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnSair:
                telaSair();
                break;
            case R.id.btnCliente:
                telaConfigConta();
                break;
            case R.id.btnOrdemServico:
                telaOrdemDeServico();
                break;
            case R.id.btnConsultarServico:
                telaConsultarServico();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference docRef = DB.collection("usuarios").document(usuarioID);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                binding.txtCliente.setText("Usuário: " + documentSnapshot
                        .getString("nome"));
            }
        });

    }

    public void telaConfigConta() {
        Intent intent = new Intent(this, TelaConfigConta.class);
        startActivity(intent);
    }

    public void telaOrdemDeServico() {
        Intent intent = new Intent(this, TelaSolicitarOrcamento.class);
        startActivity(intent);
    }

    public void telaConsultarServico() {
        Intent intent = new Intent(this, TelaConsultarServico.class);
        startActivity(intent);
    }

    public void telaSair() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Conta deslogada com sucesso", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TelaLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}