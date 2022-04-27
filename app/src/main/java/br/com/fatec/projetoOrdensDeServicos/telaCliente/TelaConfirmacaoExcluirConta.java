package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.TelaLogin;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityConfirmacaoExcluirContaBinding;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaConfirmacaoExcluirConta extends AppCompatActivity implements View.OnClickListener {
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private String usuarioID;
    private FirebaseUser usuario;
    private ActivityConfirmacaoExcluirContaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfirmacaoExcluirContaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.btnExcluir.setOnClickListener(this);
        usuario = FirebaseAuth.getInstance().getCurrentUser();
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    @Override
    public void onClick(View v) {
        excluirConta();
    }

    public void excluirConta() {
        Intent intent = getIntent();
        String nome = intent.getStringExtra(Constante.CHAVE_NOME);
        String senha = Objects.requireNonNull(binding.txtSenha.getText()).toString().trim();
        if (senha.isEmpty()) {
            binding.txtInputLayout1.setError(Constante.PREENCHA_TODOS_CAMPOS);
        } else {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(Objects.requireNonNull(usuario.getEmail()), senha);
            usuario.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            confirmarExclusao(nome);
                        else {
                            binding.txtInputLayout1.setError(Constante.SENHA_INVALIDA);
                            Objects.requireNonNull(binding.txtSenha.getText()).clear();
                        }
                    });
        }

    }

    private void confirmarExclusao(String nome) {
        DB.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AlertDialog.Builder confirmaExclusao = new AlertDialog.Builder(this);
                confirmaExclusao.setTitle(Constante.ATENCAO);
                confirmaExclusao.setMessage(Constante.CERTEZA_EXCLUIR_CONTA + nome +
                        Constante.PONTO_INTERROGACAO);
                confirmaExclusao.setPositiveButton(Constante.SIM, (dialogInterface, i) -> {
                    for (QueryDocumentSnapshot documento :
                            Objects.requireNonNull(task.getResult())) {
                        ExcluirComentario(documento);
                        DB.collection(Constante.USUARIOS).document(usuarioID)
                                .collection(Constante.ORDENS_SERVICOS)
                                .document(documento.getId()).delete();
                    }
                    deletarUsuario();
                });
                confirmaExclusao.setNegativeButton(Constante.NAO, (dialogInterface, i) -> {
                    Intent intent = new Intent(this, TelaConfigConta.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
                confirmaExclusao.setCancelable(false);
                confirmaExclusao.create().show();

            } else
                Toast.makeText(TelaConfirmacaoExcluirConta.this, Constante.ERRO_DELETAR_CONTA,
                        Toast.LENGTH_LONG).show();
        });
    }

    private void deletarUsuario() {
        DB.collection(Constante.USUARIOS).document(usuarioID).delete()
                .addOnSuccessListener(unused -> usuario.delete().addOnCompleteListener(
                        tarefaDoc -> {
                            if (tarefaDoc.isSuccessful()) {
                                binding.pBCarregar.setVisibility(View.VISIBLE);
                                binding.btnExcluir.setEnabled(false);
                                new Handler().postDelayed(() -> {
                                    Toast.makeText(TelaConfirmacaoExcluirConta.this,
                                            Constante.CONTA_DELETADO_SUCESSO, Toast.LENGTH_LONG)
                                            .show();
                                    Intent intent = new Intent(this, TelaLogin.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }, Constante.TEMPO_3SEG);
                            }
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, Constante.ERRO_DELETAR_CONTA, Toast.LENGTH_LONG)
                                .show());
    }

    private void ExcluirComentario(@NonNull QueryDocumentSnapshot documento) {
        DB.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .document(documento.getId())
                .collection(Constante.COMENTARIO)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(Constante.IDCHAT).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentoCom :
                                Objects.requireNonNull(task.getResult())) {
                            DB.collection(Constante.USUARIOS).document(usuarioID)
                                    .collection(Constante.ORDENS_SERVICOS)
                                    .document(documento.getId())
                                    .collection(Constante.COMENTARIO)
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .collection(Constante.IDCHAT)
                                    .document(documentoCom.getId()).delete();
                        }
                    }
                });
        DB.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .document(documento.getId())
                .collection(Constante.COMENTARIO)
                .document(Constante.IDCHAT)
                .collection(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentoCom :
                                Objects.requireNonNull(task.getResult())) {
                            DB.collection(Constante.USUARIOS).document(usuarioID)
                                    .collection(Constante.ORDENS_SERVICOS)
                                    .document(documento.getId())
                                    .collection(Constante.COMENTARIO)
                                    .document(Constante.IDCHAT)
                                    .collection(FirebaseAuth.getInstance().getUid())
                                    .document(documentoCom.getId()).delete();
                        }
                    }
                });
    }
}