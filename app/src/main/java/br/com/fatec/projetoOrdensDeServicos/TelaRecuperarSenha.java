package br.com.fatec.projetoOrdensDeServicos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class TelaRecuperarSenha extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText txtEmail;
    private Button btnRecuperarSenha;
    private FirebaseAuth autenticacao;
    private ProgressBar pBCarregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);
        txtEmail = findViewById(R.id.txtEmail);
        btnRecuperarSenha = findViewById(R.id.btnRecuperarSenha);
        pBCarregar = findViewById(R.id.pBCarregar);
        autenticacao = FirebaseAuth.getInstance();
        btnRecuperarSenha.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        recuperarSenha();
    }

    private void recuperarSenha() {
        String email = Objects.requireNonNull(txtEmail.getText()).toString().trim();

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
                    pBCarregar.setVisibility(View.VISIBLE);
                    btnRecuperarSenha.setEnabled(false);
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