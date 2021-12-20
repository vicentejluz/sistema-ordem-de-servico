package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.util.Mascara;

public class TelaConfigConta extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText txtNome, txtTel, txtEmail;
    private String nome;
    private Cliente cliente;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private String usuarioID, confimarSenha;
    private final FirebaseUser USUARIO = FirebaseAuth.getInstance().getCurrentUser();
    private String getNome, getTelefone, getEmail;

    private static final String TAG = "Configuração da Conta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_conta);
        txtNome = findViewById(R.id.txtNome);
        txtTel = findViewById(R.id.txtTel);
        txtEmail = findViewById(R.id.txtEmail);
        Button btnEditar = findViewById(R.id.btnEditar);
        Button btnAlterarSenha = findViewById(R.id.btnAlterarSenha);
        TextView txtExcluir = findViewById(R.id.txtExcluir);
        txtExcluir.setOnClickListener(this);
        btnEditar.setOnClickListener(this);
        btnAlterarSenha.setOnClickListener(this);
        txtTel.addTextChangedListener(Mascara.insert(Mascara.MaskType.TEL, txtTel));
    }

    @Override
    protected void onStart() {
        super.onStart();
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference docRef = DB.collection("usuarios").document(usuarioID);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                txtEmail.setText(USUARIO.getEmail());
                txtNome.setText(documentSnapshot.getString("nome"));
                txtTel.setText(documentSnapshot.getString("telefone"));
                getNome = documentSnapshot.getString("nome");
                getTelefone = documentSnapshot.getString("telefone");
                getEmail = USUARIO.getEmail();
            } else {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.btnEditar:
                ConfirmarAlteracao();
                break;
            case R.id.btnAlterarSenha:
                telaAlterarSenha();
                break;
            case R.id.txtExcluir:
                confirmarExclucao();
                break;
        }
    }

    private void alterarDadosUsuario() {
        DocumentReference docRef = DB.collection("usuarios").document(usuarioID);
        assert USUARIO != null;
        USUARIO.updateEmail(cliente.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Endereço de e-mail do usuário atualizado.");
                docRef.update("nome", cliente.getNome());
                docRef.update("email", cliente.getEmail());
                docRef.update("telefone", cliente.getTelefone());
                Toast.makeText(this, "Atualizado com sucesso",
                        Toast.LENGTH_LONG).show();
            } else {
                String erro;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    erro = "E-mail Inválido";
                } catch (Exception e) {
                    erro = "Falha ao atualizar o perfil.";
                }
                Log.w(TAG, "Erro: Atualização falhou", task.getException());
                Toast.makeText(this, erro, Toast.LENGTH_LONG).show();
                onStart();
            }
        });
    }

    private void validandoUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_senha, null);
        builder.setTitle("Digite sua Senha:");
        builder.setView(view);
        TextInputLayout txtInputLayout1 = view.findViewById(R.id.txtInputLayout1);
        TextInputEditText txtSenha = view.findViewById(R.id.txtSenha);
        Button btnConfirmar = view.findViewById(R.id.btnConfirmar);
        TextView txtCancelar = view.findViewById(R.id.txtCancelar);
        Dialog alterarPerfil = builder.create();
        alterarPerfil.create();
        btnConfirmar.setOnClickListener(v1 -> {
            confimarSenha = Objects.requireNonNull(txtSenha.getText()).toString().trim();
            if (confimarSenha.isEmpty()) {
                txtInputLayout1.setError("Preencha o campo");
            } else {
                AuthCredential credential = EmailAuthProvider
                        .getCredential(Objects.requireNonNull(USUARIO.getEmail()), confimarSenha);
                USUARIO.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                alterarDadosUsuario();
                                Log.d(TAG, "Usuário reautenticado.");
                                alterarPerfil.dismiss();
                            } else {
                                txtInputLayout1.setError("Senha inválida");
                                Objects.requireNonNull(txtSenha.getText()).clear();
                            }
                        });
            }
        });
        txtCancelar.setOnClickListener(v12 -> {
            alterarPerfil.dismiss();
            onStart();
        });
        alterarPerfil.setCancelable(false);
        alterarPerfil.show();
    }

    private void ConfirmarAlteracao() {
        nome = Objects.requireNonNull(txtNome.getText()).toString().trim();
        String telefone = Objects.requireNonNull(txtTel.getText()).toString().trim();
        String email = Objects.requireNonNull(txtEmail.getText()).toString().trim();
        cliente = new Cliente(nome, email, telefone);
        if (cliente.getNome().isEmpty() || cliente.getEmail().isEmpty()
                || cliente.getTelefone().isEmpty()) {
            Toast.makeText(this, "ERRO - Preencha todos os campos",
                    Toast.LENGTH_LONG).show();
        } else {
            if (telefone.length() < 15) {
                Toast.makeText(this, "Telefone Inválido",
                        Toast.LENGTH_LONG).show();
            } else {
                if (nome.equals(getNome) && telefone.equals(getTelefone) && email.equals(getEmail)) {
                    Toast.makeText(this, "Altere nome ou telefone ou e-mail do seu " +
                                    "perfil",
                            Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder alteraDados = new AlertDialog.Builder(this);
                    alteraDados.setTitle("Atenção!!");
                    alteraDados.setMessage("Tem certeza que deseja alterar seu perfil?");
                    alteraDados.setCancelable(false);
                    alteraDados.setPositiveButton("Sim", (dialogInterface, i) -> validandoUsuario());
                    alteraDados.setNegativeButton("Não", (dialog, which) -> onStart());
                    alteraDados.setCancelable(false);
                    alteraDados.create().show();
                }
            }
        }
    }

    public void confirmarExclucao() {
        nome = Objects.requireNonNull(txtNome.getText()).toString().trim();
        Intent intent = new Intent(this,
                TelaConfirmacaoExcluirConta.class);
        intent.putExtra("chaveNome", nome);
        startActivity(intent);

    }

    public void telaAlterarSenha() {
        Intent intent = new Intent(this, TelaAlterarSenha.class);
        startActivity(intent);
    }
}