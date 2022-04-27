package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityConfigContaBinding;
import br.com.fatec.projetoOrdensDeServicos.databinding.DialogSenhaBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class TelaConfigConta extends AppCompatActivity {
    private ActivityConfigContaBinding binding;
    private DialogSenhaBinding dialogBinding;
    private String nome;
    private Cliente cliente;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private String usuarioID, confimarSenha;
    private final FirebaseUser USUARIO = FirebaseAuth.getInstance().getCurrentUser();
    private String getNome, getTelefone, getEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfigContaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnExcluir.setOnClickListener(v -> confirmarExclucao());
        binding.btnEditar.setOnClickListener(v -> ConfirmarAlteracao());
        binding.btnAlterarSenha.setOnClickListener(v -> telaAlterarSenha());
        binding.txtTel.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL, binding.txtTel));
    }

    @Override
    protected void onStart() {
        super.onStart();
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference docRef = DB.collection(Constante.USUARIOS).document(usuarioID);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                getNome = documentSnapshot.getString(Constante.NOME);
                getTelefone = documentSnapshot.getString(Constante.TELEFONE);
                getEmail = USUARIO.getEmail();
                binding.txtEmail.setText(getEmail);
                binding.txtNome.setText(documentSnapshot.getString(getNome));
                binding.txtTel.setText(documentSnapshot.getString(getTelefone));
            } else {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }

    private void alterarDadosUsuario() {
        DocumentReference docRef = DB.collection(Constante.USUARIOS).document(usuarioID);
        USUARIO.updateEmail(cliente.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                docRef.update(Constante.NOME, cliente.getNome());
                docRef.update(Constante.EMAIL, cliente.getEmail());
                docRef.update(Constante.TELEFONE, cliente.getTelefone());
                Toast.makeText(this, Constante.ATUALIZADO_SUCESSO,
                        Toast.LENGTH_LONG).show();
            } else {
                String erro;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    erro = Constante.EMAIL_INVALIDO;
                } catch (Exception e) {
                    erro = Constante.FALHA_ATUALIZAR_PERFIL;
                }
                Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
                onStart();
            }
        });
    }

    private void validandoUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogBinding = DialogSenhaBinding.inflate(getLayoutInflater());
        View view = dialogBinding.getRoot();
        builder.setTitle(Constante.DIGITE_SENHA);
        builder.setView(view);
        Dialog alterarPerfil = builder.create();
        alterarPerfil.create();
        dialogBinding.btnConfirmar.setOnClickListener(v1 -> {
            confimarSenha = Objects.requireNonNull(dialogBinding.txtSenha.getText()).toString()
                    .trim();
            if (confimarSenha.isEmpty()) {
                dialogBinding.txtInputLayout1.setError(Constante.PREENCHA_CAMPO);
            } else {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(Objects.requireNonNull(USUARIO.getEmail()), confimarSenha);
                USUARIO.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                alterarDadosUsuario();
                                alterarPerfil.dismiss();
                            } else {
                                dialogBinding.txtInputLayout1.setError(Constante.SENHA_INVALIDA);
                                Objects.requireNonNull(dialogBinding.txtSenha.getText()).clear();
                            }
                        });
            }
        });
        dialogBinding.btnCancelar.setOnClickListener(v12 -> {
            alterarPerfil.dismiss();
            onStart();
        });
        alterarPerfil.setCancelable(false);
        alterarPerfil.show();
    }

    private void ConfirmarAlteracao() {
        nome = Objects.requireNonNull(binding.txtNome.getText()).toString().trim();
        String telefone = Objects.requireNonNull(binding.txtTel.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.txtEmail.getText()).toString().trim();
        cliente = new Cliente(nome, email, telefone);
        if (cliente.getNome().isEmpty() || cliente.getEmail().isEmpty()
                || cliente.getTelefone().isEmpty()) {
            Toast.makeText(this, Constante.PREENCHA_TODOS_CAMPOS,
                    Toast.LENGTH_LONG).show();
        } else {
            if (telefone.length() < 15) {
                Toast.makeText(this, Constante.TELEFONE_INVALIDO,
                        Toast.LENGTH_LONG).show();
            } else {
                if (nome.equals(getNome) && telefone.equals(getTelefone) && email.equals(getEmail)) {
                    Toast.makeText(this, Constante.ALTERE_NOME_TELEFONE_EMAIL,
                            Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder alteraDados = new AlertDialog.Builder(this);
                    alteraDados.setTitle(Constante.ATENCAO);
                    alteraDados.setMessage(Constante.CERTEZA_ALTERAR_PERFIL);
                    alteraDados.setCancelable(false);
                    alteraDados.setPositiveButton(Constante.SIM, (dialogInterface, i) ->
                            validandoUsuario());
                    alteraDados.setNegativeButton(Constante.NAO, (dialog, which) -> onStart());
                    alteraDados.setCancelable(false);
                    alteraDados.create().show();
                }
            }
        }
    }

    public void confirmarExclucao() {
        nome = Objects.requireNonNull(binding.txtNome.getText()).toString().trim();
        Intent intent = new Intent(this,
                TelaConfirmacaoExcluirConta.class);
        intent.putExtra(Constante.CHAVE_NOME, nome);
        startActivity(intent);

    }

    public void telaAlterarSenha() {
        Intent intent = new Intent(this, TelaAlterarSenha.class);
        startActivity(intent);
    }
}