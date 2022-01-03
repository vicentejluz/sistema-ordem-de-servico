package br.com.fatec.projetoOrdensDeServicos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityRecuperarSenhaBinding;

public class TelaRecuperarSenha extends AppCompatActivity implements View.OnClickListener {
    private ActivityRecuperarSenhaBinding binding;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecuperarSenhaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        autenticacao = FirebaseAuth.getInstance();
        binding.btnRecuperarSenha.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        recuperarSenha();
    }

    private void recuperarSenha() {
        String email = Objects.requireNonNull(binding.txtEmail.getText())
                .toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "ERRO - Preencha o campo E-mail",
                    Toast.LENGTH_LONG).show();
        } else {
            enviarEmail(email);
        }
    }

    private void enviarEmail(String email) {
        autenticacao.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    binding.pBCarregar.setVisibility(View.VISIBLE);
                    binding.btnRecuperarSenha.setEnabled(false);
                    new Handler().postDelayed(() -> {
                        Toast.makeText(TelaRecuperarSenha.this,
                                "Email de recuperação de senha enviado",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new
                                Intent(TelaRecuperarSenha.this, TelaLogin.class);
                        startActivity(intent);
                        finish();
                    }, 3000);
                })
                .addOnFailureListener(e -> Toast.makeText(TelaRecuperarSenha.this,
                        "E-mail inválido ou não encontrado",
                        Toast.LENGTH_LONG).show()
                );
    }
}