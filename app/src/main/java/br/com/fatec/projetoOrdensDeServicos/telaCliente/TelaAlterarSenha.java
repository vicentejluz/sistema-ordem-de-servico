package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityAlterarSenhaBinding;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaAlterarSenha extends AppCompatActivity implements View.OnClickListener {
    private ActivityAlterarSenhaBinding binding;
    private String novaSenha;
    private FirebaseUser usuario;

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
        novaSenha = Objects.requireNonNull(binding.txtNovaSenha.getText()).toString();
        String confimarSenha = Objects.requireNonNull(binding.txtConfirmarSenha.getText()).toString();
        String senhaAntiga = Objects.requireNonNull(binding.txtSenhaAntiga.getText()).toString();
        if (novaSenha.isEmpty() || confimarSenha.isEmpty() || senhaAntiga.isEmpty()) {
            Toast.makeText(this, Constante.PREENCHA_TODOS_CAMPOS,
                    Toast.LENGTH_LONG).show();
        } else {
            if (!novaSenha.equals(confimarSenha)) {
                Toast.makeText(this,
                        Constante.SENHAS_NAO_BATEM, Toast.LENGTH_SHORT).show();
            } else {
                if (novaSenha.equals(senhaAntiga)) {
                    Toast.makeText(this, Constante.IGUAL_SENHA_ANTIGA, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(Objects.requireNonNull(usuario.getEmail()), senhaAntiga);
                    usuario.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    confimarAlteracaoSenha();
                                } else {
                                    Toast.makeText(TelaAlterarSenha.this,
                                            Constante.SENHA_INVALIDA, Toast.LENGTH_LONG)
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
        alterarSenha.setTitle(Constante.ATENCAO);
        alterarSenha.setMessage(Constante.CERTEZA_ALTERAR_SENHA);
        alterarSenha.setPositiveButton(Constante.SIM, (dialogInterface, i) -> usuario
                .updatePassword(novaSenha).addOnCompleteListener(tarefa -> {
                    if (tarefa.isSuccessful()) {
                        binding.btnConfirmar.setEnabled(false);
                        binding.pBCarregar.setVisibility(View.VISIBLE);
                        binding.btnConfirmar.setEnabled(false);
                        new Handler().postDelayed(() -> {
                            Toast.makeText(TelaAlterarSenha.this,
                                    Constante.SENHA_ALTERADO_SUCESSO, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(TelaAlterarSenha.this,
                                    TelaConfigConta.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }, Constante.TEMPO_3SEG);
                    } else {
                        String erro;
                        try {
                            throw Objects.requireNonNull(tarefa.getException());
                        } catch (FirebaseAuthWeakPasswordException e) {
                            erro = Constante.MINIMO_6_CARAC;
                        } catch (Exception e) {
                            erro = Constante.FALHA_ATUALIZAR_SENHA;
                        }
                        Toast.makeText(TelaAlterarSenha.this, erro, Toast.LENGTH_LONG)
                                .show();
                        Objects.requireNonNull(binding.txtNovaSenha.getText()).clear();
                        Objects.requireNonNull(binding.txtConfirmarSenha.getText()).clear();
                        Objects.requireNonNull(binding.txtSenhaAntiga.getText()).clear();
                        binding.txtSenhaAntiga.requestFocus();
                    }
                }));
        alterarSenha.setNegativeButton(Constante.NAO, null);
        alterarSenha.setCancelable(false);
        alterarSenha.create().show();
    }
}
