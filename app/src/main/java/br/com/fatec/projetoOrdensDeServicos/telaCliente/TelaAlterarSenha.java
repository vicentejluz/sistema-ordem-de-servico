package br.com.fatec.projetoOrdensDeServicos.telaCliente;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityAlterarSenhaBinding;

public class TelaAlterarSenha extends AppCompatActivity implements View.OnClickListener {
    private ActivityAlterarSenhaBinding binding;
    private String novaSenha;
    private FirebaseUser usuario;
    private static final String TAG = "Alterar senha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlterarSenhaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnConfirmar.setOnClickListener(this);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        alterarSenhaUsuario();
    }

    private void alterarSenhaUsuario() {
        novaSenha = Objects.requireNonNull(binding.txtNovaSenha.getText()).toString().trim();
        String confimarSenha = Objects.requireNonNull(binding.txtConfirmarSenha.getText()).toString()
                .trim();
        String senhaAntiga = Objects.requireNonNull(binding.txtSenhaAntiga.getText()).toString()
                .trim();
        if (novaSenha.isEmpty() || confimarSenha.isEmpty() || senhaAntiga.isEmpty()) {
            Toast.makeText(this, "ERRO - Preencha todos os campos",
                    Toast.LENGTH_LONG).show();
        } else {
            if (!novaSenha.equals(confimarSenha)) {
                Toast.makeText(this,
                        "ERRO: As senhas não batem!!", Toast.LENGTH_SHORT).show();
            } else {
                if (novaSenha.equals(senhaAntiga)) {
                    Toast.makeText(this,
                            "Senha inválida - igual senha antiga!!", Toast.LENGTH_SHORT).show();
                } else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(Objects.requireNonNull(usuario.getEmail()), senhaAntiga);
                    usuario.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    confimarAlteracaoSenha();
                                    Log.d(TAG, "User re-authenticated.");
                                } else {
                                    Toast.makeText(TelaAlterarSenha.this,
                                            "Senha Inválida!", Toast.LENGTH_LONG)
                                            .show();
                                    Objects.requireNonNull(binding.txtNovaSenha.getText()).clear();
                                    Objects.requireNonNull(binding.txtConfirmarSenha.getText())
                                            .clear();
                                    Objects.requireNonNull(binding.txtSenhaAntiga.getText())
                                            .clear();
                                    binding.txtSenhaAntiga.requestFocus();
                                }
                            });
                }

            }

        }
    }

    private void confimarAlteracaoSenha() {
        AlertDialog.Builder alterarSenha = new AlertDialog.Builder(TelaAlterarSenha.this);
        alterarSenha.setTitle("Atenção!!");
        alterarSenha.setMessage("Tem certeza que deseja alterar a senha?");
        alterarSenha.setPositiveButton("Sim", (dialogInterface, i) -> usuario
                .updatePassword(novaSenha).addOnCompleteListener(tarefa -> {
                    if (tarefa.isSuccessful()) {
                        Log.d(TAG, "Senha Alterada");
                        binding.btnConfirmar.setEnabled(false);
                        binding.pBCarregar.setVisibility(View.VISIBLE);
                        binding.btnConfirmar.setEnabled(false);
                        new Handler().postDelayed(() -> {
                            Toast.makeText(TelaAlterarSenha.this,
                                    "Senha alterada com sucesso", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(TelaAlterarSenha.this,
                                    TelaConfigConta.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }, 3000);
                    } else {
                        String erro;
                        try {
                            throw Objects.requireNonNull(tarefa.getException());
                        } catch (FirebaseAuthWeakPasswordException e) {
                            erro = "Digite uma senha com no mínimo 6 caracteres";
                        } catch (Exception e) {
                            erro = "Falha ao atualizar a Senha.";
                        }
                        Log.w(TAG, "Erro: Atualização falhou", tarefa.getException());
                        Toast.makeText(TelaAlterarSenha.this, erro, Toast.LENGTH_LONG)
                                .show();
                        Objects.requireNonNull(binding.txtNovaSenha.getText()).clear();
                        Objects.requireNonNull(binding.txtConfirmarSenha.getText()).clear();
                        Objects.requireNonNull(binding.txtSenhaAntiga.getText()).clear();
                        binding.txtSenhaAntiga.requestFocus();
                    }
                }));
        alterarSenha.setNegativeButton("Não", null);
        alterarSenha.setCancelable(false);
        alterarSenha.create().show();
    }
}
