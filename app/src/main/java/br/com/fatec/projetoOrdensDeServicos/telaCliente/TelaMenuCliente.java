package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.TelaLogin;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityMenuClienteBinding;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaMenuCliente extends AppCompatActivity {
    private ActivityMenuClienteBinding binding;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuClienteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TooltipCompat.setTooltipText(binding.btnSair, Constante.DESLOGAR);
        binding.btnSair.setOnClickListener(v -> telaSair());
        binding.btnCliente.setOnClickListener(v -> telaConfigConta());
        binding.btnOrdemServico.setOnClickListener(v -> telaOrdemDeServico());
        binding.btnConsultarServico.setOnClickListener(v -> telaConsultarServico());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference docRef = DB.collection(Constante.USUARIOS).document(usuarioID);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                String nomeUsuario = Constante.USUARIO + documentSnapshot.getString(Constante.NOME);
                binding.txtCliente.setText(nomeUsuario);
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
        Toast.makeText(this, Constante.CONTA_DESLOGADA_SUCESSO, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TelaLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}