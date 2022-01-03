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
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class TelaCadastro extends AppCompatActivity implements View.OnClickListener {
    private Cliente cliente;
    private FirebaseAuth cadastrar;
    private FirebaseFirestore db;
    private Map<String, Object> data;
    private String usuarioID;
    private ActivityCadastroBinding binding;
    private static final String TAG = "Cadastro";

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
        String privilegio = "Cliente";
        String statusConta = "Desbloqueado";
        cliente = new Cliente(nome, email, telefone, statusConta);
        if (cliente.getNome().isEmpty() || cliente.getTelefone().isEmpty()
                || cliente.getEmail().isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(TelaCadastro.this, "ERRO - Preencha todos os campos",
                    Toast.LENGTH_LONG).show();
        } else {
            if (telefone.length() < 15) {
                Toast.makeText(TelaCadastro.this, "Telefone Inválido",
                        Toast.LENGTH_LONG).show();
            } else {
                if (!senha.equals(confirmarSenha)) {
                    Toast.makeText(TelaCadastro.this,
                            "ERRO: As senhas não batem!!", Toast.LENGTH_SHORT).show();
                } else {
                    cadastrar.createUserWithEmailAndPassword(cliente.getEmail(), senha)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    data.put("nome", cliente.getNome());
                                    data.put("email", cliente.getEmail());
                                    data.put("telefone", cliente.getTelefone());
                                    data.put("statusConta", cliente.getStatusConta());
                                    data.put("privilegio", privilegio);
                                    usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance()
                                            .getCurrentUser()).getUid();
                                    DocumentReference docRef = db.collection("usuarios")
                                            .document(usuarioID);
                                    docRef.set(data)
                                            .addOnSuccessListener(unused ->
                                                    Log.d(TAG, "ID gerado: " + docRef.getId()))
                                            .addOnFailureListener(e ->
                                                    Log.w(TAG, "Erro ao adicionar o documento",
                                                            e));

                                    binding.pBCarregar.setVisibility(View.VISIBLE);
                                    binding.btnCadastrar.setEnabled(false);
                                    new Handler().postDelayed(() -> {
                                        Toast.makeText(TelaCadastro.this,
                                                "Registrado com sucesso:"
                                                        + cliente.getEmail(), Toast.LENGTH_LONG)
                                                .show();
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(TelaCadastro.this,
                                                TelaLogin.class);
                                        startActivity(intent);
                                        finish();
                                    }, 3000);
                                } else {
                                    String erro;
                                    try {
                                        throw Objects.requireNonNull(task.getException());
                                    } catch (FirebaseAuthWeakPasswordException e) {
                                        erro = "Digite uma senha com no mínimo 6 caracteres";

                                    } catch (FirebaseAuthUserCollisionException e) {
                                        erro = "Usuário já existe";

                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        erro = "E-mail Inválido";

                                    } catch (Exception e) {
                                        erro = "Erro ao cadastrar usário";
                                    }

                                    Toast.makeText(TelaCadastro.this, erro,
                                            Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "CriarUsuarioFalhou: " + erro,
                                            task.getException());
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
}