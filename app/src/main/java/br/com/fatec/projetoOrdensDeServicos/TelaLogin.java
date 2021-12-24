package br.com.fatec.projetoOrdensDeServicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaMenuAdmin;
import br.com.fatec.projetoOrdensDeServicos.telaCliente.TelaMenuCliente;

public class TelaLogin extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText txtEmail, txtSenha;
    private Button btnEntrar, btnCadastro, btnRecuperarSenha;
    private String email, privilegio, statusConta;
    private FirebaseAuth autenticacao;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private static final String TAG = "Login";
    private ProgressBar pBCarregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_login);
        pBCarregar = findViewById(R.id.pBCarregar);
        txtEmail = findViewById(R.id.txtEmail);
        txtSenha = findViewById(R.id.txtSenha);
        btnRecuperarSenha = findViewById(R.id.btnRecuperarSenha);
        btnCadastro = findViewById(R.id.btnCadastro);
        btnEntrar = findViewById(R.id.btnEntrar);
        autenticacao = FirebaseAuth.getInstance();
        btnRecuperarSenha.setOnClickListener(this);
        btnCadastro.setOnClickListener(this);
        btnEntrar.setOnClickListener(this);
    }

    private void logarUsuario() {
        email = Objects.requireNonNull(txtEmail.getText()).toString().trim();
        String senha = Objects.requireNonNull(txtSenha.getText()).toString().trim();
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(TelaLogin.this, "ERRO - Preencha todos os campos",
                    Toast.LENGTH_LONG).show();
        } else {
            autenticacao.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            DocumentReference docRef = DB.collection("usuarios")
                                    .document(Objects.requireNonNull(autenticacao.getCurrentUser())
                                            .getUid());
                            docRef.get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            assert document != null;
                                            if (document.exists()) {
                                                privilegio = document.getString("privilegio");
                                                statusConta = document.getString("statusConta");
                                                assert statusConta != null;
                                                if (statusConta.equalsIgnoreCase("Bloqueado")) {
                                                    Toast.makeText(TelaLogin.this, "Usuário " +
                                                            "Bloqueado", Toast.LENGTH_LONG).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                } else {
                                                    assert privilegio != null;
                                                    if (privilegio.equalsIgnoreCase("Cliente")) {
                                                        mudancaActivity(TelaMenuCliente.class);
                                                    } else {
                                                        mudancaActivity(TelaMenuAdmin.class);
                                                    }
                                                }
                                            } else {
                                                Log.d(TAG, "No such document");
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task1.getException());
                                        }
                                    });

                        } else {
                            String erro;
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (Exception e) {
                                erro = "Email ou senha inválido";
                            }
                            Log.w(TAG, "Autenticação: falhou", task.getException());
                            Toast.makeText(TelaLogin.this, erro, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnCadastro:
                telaCadastro();
                break;
            case R.id.btnEntrar:
                InputMethodManager inputManager = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                logarUsuario();
                break;
            case R.id.btnRecuperarSenha:
                telarecuperarSenha();
                break;
        }
    }

    public void telaCadastro() {
        Objects.requireNonNull(txtEmail.getText()).clear();
        Objects.requireNonNull(txtSenha.getText()).clear();
        txtEmail.clearFocus();
        txtSenha.clearFocus();
        Intent intent = new Intent(this, TelaCadastro.class);
        startActivity(intent);
    }

    public void telarecuperarSenha() {
        Objects.requireNonNull(txtEmail.getText()).clear();
        Objects.requireNonNull(txtSenha.getText()).clear();
        txtEmail.clearFocus();
        txtSenha.clearFocus();
        Intent intent = new Intent(this, TelaRecuperarSenha.class);
        startActivity(intent);
    }

    public void mudancaActivity(Class<?> tela) {
        pBCarregar.setVisibility(View.VISIBLE);
        btnEntrar.setEnabled(false);
        btnCadastro.setEnabled(false);
        btnRecuperarSenha.setEnabled(false);
        new Handler().postDelayed(() -> {
            Toast.makeText(TelaLogin.this, "Logado com sucesso: " + email,
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(TelaLogin.this, tela);
            startActivity(intent);
            finish();
        }, 3000);
    }
}