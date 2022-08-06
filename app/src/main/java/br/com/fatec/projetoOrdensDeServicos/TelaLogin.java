package br.com.fatec.projetoOrdensDeServicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityLoginBinding;
import br.com.fatec.projetoOrdensDeServicos.telaAdmin.TelaMenuAdmin;
import br.com.fatec.projetoOrdensDeServicos.telaCliente.TelaMenuCliente;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaLogin extends AppCompatActivity implements View.OnClickListener {
    private String privilegio;
    private String statusConta;
    private FirebaseAuth autenticacao;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        autenticacao = FirebaseAuth.getInstance();
        binding.btnRecuperarSenha.setOnClickListener(v -> telarecuperarSenha());
        binding.btnCadastro.setOnClickListener(v -> telaCadastro());
        binding.btnEntrar.setOnClickListener(this);
    }

    private void logarUsuario() {
        String email = Objects.requireNonNull(binding.txtEmail.getText()).toString().trim();
        String senha = Objects.requireNonNull(binding.txtSenha.getText()).toString();
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(TelaLogin.this, Constante.PREENCHA_TODOS_CAMPOS,
                    Toast.LENGTH_LONG).show();
        } else {
            autenticacao.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            DocumentReference docRef = DB.collection(Constante.USUARIOS)
                                    .document(Objects.requireNonNull(autenticacao.getCurrentUser())
                                            .getUid());
                            docRef.get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document = task1.getResult();
                                            if (Objects.requireNonNull(document).exists()) {
                                                privilegio = document.getString(
                                                        Constante.PRIVILEGIO);
                                                statusConta = document.getString(
                                                        Constante.STATUS_CONTA);
                                                if (Objects.requireNonNull(statusConta)
                                                        .equalsIgnoreCase(Constante.BLOQUEADO)) {
                                                    Toast.makeText(TelaLogin.this,
                                                            Constante.USUARIO_BLOQUEADO, Toast
                                                                    .LENGTH_LONG).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                } else {
                                                    if (privilegio.equalsIgnoreCase(
                                                            Constante.CLIENTE)) {
                                                        mudancaActivity(TelaMenuCliente.class);
                                                    } else {
                                                        mudancaActivity(TelaMenuAdmin.class);
                                                    }
                                                }
                                            } else {
                                                Log.d(Constante.TAG_LOGIN,
                                                        Constante.DOCUMENTO_NAO_EXISTE);
                                            }
                                        } else {
                                            Log.d(Constante.TAG_LOGIN, Constante.FALHA_PROCURAR,
                                                    task1.getException());
                                        }
                                    });

                        } else {
                            String erro;
                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (Exception e) {
                                erro = Constante.EMAIL_OU_SENHA_INVALIDO;
                            }
                            Log.w(Constante.TAG_LOGIN, Constante.AUTENTICACAO_FALHOU,
                                    task.getException());
                            Toast.makeText(TelaLogin.this, erro, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        logarUsuario();
    }

    public void telaCadastro() {
        Objects.requireNonNull(binding.txtEmail.getText()).clear();
        Objects.requireNonNull(binding.txtSenha.getText()).clear();
        binding.txtEmail.clearFocus();
        binding.txtSenha.clearFocus();
        Intent intent = new Intent(this, TelaCadastro.class);
        startActivity(intent);
    }

    public void telarecuperarSenha() {
        Objects.requireNonNull(binding.txtEmail.getText()).clear();
        Objects.requireNonNull(binding.txtSenha.getText()).clear();
        binding.txtEmail.clearFocus();
        binding.txtSenha.clearFocus();
        Intent intent = new Intent(this, TelaRecuperarSenha.class);
        startActivity(intent);
    }

    public void mudancaActivity(Class<?> tela) {
        binding.pBCarregar.setVisibility(View.VISIBLE);
        binding.btnEntrar.setEnabled(false);
        binding.btnCadastro.setEnabled(false);
        binding.btnRecuperarSenha.setEnabled(false);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(TelaLogin.this, tela);
            startActivity(intent);
            finish();
        }, Constante.TEMPO_3SEG);
    }
}