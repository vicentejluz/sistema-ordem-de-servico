package br.com.fatec.projetoOrdensDeServicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityCadastroBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class TelaCadastro extends AppCompatActivity implements View.OnClickListener {
    private Cliente cliente;
    private FirebaseAuth cadastrar;
    private FirebaseFirestore db;
    private Map<String, Object> data;
    private String usuarioID;
    private ActivityCadastroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        cadastrar = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        data = new HashMap<>();
        TooltipCompat.setTooltipText(binding.imBVoltar, Constante.VOLTAR);
        binding.imBVoltar.setOnClickListener(v -> telaLogin());
        binding.btnCadastrar.setOnClickListener(this);
        binding.txtTel.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL, binding.txtTel));
    }

    private void registrarUsuario() {
        String nome = Objects.requireNonNull(binding.txtNome.getText()).toString().trim();
        String telefone = Objects.requireNonNull(binding.txtTel.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.txtEmail.getText()).toString().trim();
        String senha = Objects.requireNonNull(binding.txtSenha.getText()).toString().trim();
        String confirmarSenha = Objects.requireNonNull(binding.txtConfirmarSenha.getText())
                .toString().trim();
        cliente = new Cliente(nome, email, telefone, Constante.DESBLOQUEADO);
        if (cliente.getNome().isEmpty() || cliente.getTelefone().isEmpty()
                || cliente.getEmail().isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(TelaCadastro.this, Constante.PREENCHA_TODOS_CAMPOS,
                    Toast.LENGTH_LONG).show();
        } else {
            if (telefone.length() < 15) {
                Toast.makeText(TelaCadastro.this, Constante.TELEFONE_INVALIDO,
                        Toast.LENGTH_LONG).show();
            } else {
                if (!senha.equals(confirmarSenha)) {
                    Toast.makeText(TelaCadastro.this,
                            Constante.SENHAS_NAO_BATEM, Toast.LENGTH_SHORT).show();
                } else {
                    cadastrar.createUserWithEmailAndPassword(cliente.getEmail(), senha)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    data.put(Constante.NOME, cliente.getNome());
                                    data.put(Constante.EMAIL, cliente.getEmail());
                                    data.put(Constante.TELEFONE, cliente.getTelefone());
                                    data.put(Constante.STATUS_CONTA, cliente.getStatusConta());
                                    data.put(Constante.PRIVILEGIO, Constante.CLIENTE);
                                    usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance()
                                            .getCurrentUser()).getUid();
                                    DocumentReference docRef = db.collection(Constante.USUARIOS)
                                            .document(usuarioID);
                                    docRef.set(data)
                                            .addOnSuccessListener(unused ->
                                                    Log.d(Constante.TAG_CADASTRO,
                                                            Constante.ID_GERADO + docRef.getId()))
                                            .addOnFailureListener(e ->
                                                    Log.w(Constante.TAG_CADASTRO,
                                                            Constante.ERRO_ADD_DOCUMENTO, e));

                                    binding.pBCarregar.setVisibility(View.VISIBLE);
                                    binding.btnCadastrar.setEnabled(false);
                                    new Handler().postDelayed(() -> {
                                        Toast.makeText(TelaCadastro.this,
                                                Constante.REGISTRADO_SUCESSO
                                                        + cliente.getEmail(), Toast.LENGTH_LONG)
                                                .show();
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(TelaCadastro.this,
                                                TelaLogin.class);
                                        startActivity(intent);
                                        finish();
                                    }, Constante.TEMPO_3SEG);
                                } else {
                                    String erro;
                                    try {
                                        throw Objects.requireNonNull(task.getException());
                                    } catch (FirebaseAuthWeakPasswordException e) {
                                        erro = Constante.MINIMO_6_CARAC;

                                    } catch (FirebaseAuthUserCollisionException e) {
                                        erro = Constante.USUARIO_JA_EXISTE;

                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        erro = Constante.EMAIL_INVALIDO;

                                    } catch (Exception e) {
                                        erro = Constante.ERRO_CADASTRAR_USUARIO;
                                    }

                                    Toast.makeText(TelaCadastro.this, erro,
                                            Toast.LENGTH_SHORT).show();
                                    Log.w(Constante.TAG_CADASTRO, Constante.CRIAR_USUARIO_FALHOU
                                            + erro, task.getException());
                                }
                            });
                }
            }
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager
                .HIDE_NOT_ALWAYS);
        registrarUsuario();
    }

    public void telaLogin() {
        onBackPressed();
    }
}