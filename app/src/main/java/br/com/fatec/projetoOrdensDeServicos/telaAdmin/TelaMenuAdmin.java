package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaLogin;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityMenuAdminBinding;

public class TelaMenuAdmin extends AppCompatActivity implements View.OnClickListener {
    ActivityMenuAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMenuAdminBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TooltipCompat.setTooltipText(binding.btnSair, "Deslogar");
        binding.btnSair.setOnClickListener(this);
        binding.btnListarCliente.setOnClickListener(this);
        binding.btnConsultarServico.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnSair:
                telaSair();
                break;
            case R.id.btnListarCliente:
                telaListarCliente();
                break;
            case R.id.btnConsultarServico:
                telaConsultarServico();
                break;
        }
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
        Toast.makeText(this, "Conta deslogada com sucesso", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TelaLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}