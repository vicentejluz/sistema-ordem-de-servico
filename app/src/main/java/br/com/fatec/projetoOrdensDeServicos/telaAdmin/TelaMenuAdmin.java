package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import br.com.fatec.projetoOrdensDeServicos.TelaLogin;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityMenuAdminBinding;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaMenuAdmin extends AppCompatActivity {
    ActivityMenuAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuAdminBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TooltipCompat.setTooltipText(binding.btnSair, Constante.DESLOGAR);
        binding.btnSair.setOnClickListener(v -> telaSair());
        binding.btnListarCliente.setOnClickListener(v -> telaListarCliente());
        binding.btnConsultarServico.setOnClickListener(v -> telaConsultarServico());
    }

    public void telaListarCliente() {
        Intent intent = new Intent(this, TelaListarCliente.class);
        startActivity(intent);
    }

    public void telaConsultarServico() {
        Intent intent = new Intent(this, TelaConsultarServicoAdmin.class);
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