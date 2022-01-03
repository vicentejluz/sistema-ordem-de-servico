package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.TelaLogin;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityConfirmacaoExcluirContaBinding;

public class TelaConfirmacaoExcluirConta extends AppCompatActivity implements View.OnClickListener {
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private String usuarioID;
    private FirebaseUser usuario;
    private ActivityConfirmacaoExcluirContaBinding binding;
    private final String IDCHAT = "pb6IdWjCKogMvZlnpH4bl13lCM22AD";
    private static final String TAG = "Confirmação de Excluir";

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
        String nome = intent.getStringExtra("chaveNome");
        String senha = Objects.requireNonNull(binding.txtSenha.getText()).toString().trim();
        if (senha.isEmpty()) {
            binding.txtInputLayout1.setError("ERRO - Preencha o campo");
        } else {
            AuthCredential credential = EmailAuthProvider
                    .getCredential(Objects.requireNonNull(usuario.getEmail()), senha);
            usuario.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            confirmarExclusao(nome);
                            Log.d(TAG, "Usuário reautenticado.");
                        } else {
                            binding.txtInputLayout1.setError("Senha inválida");
                            Objects.requireNonNull(binding.txtSenha.getText()).clear();
                        }
                    });
        }

    }

    private void confirmarExclusao(String nome) {
        DB.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AlertDialog.Builder confirmaExclusao = new AlertDialog.Builder(this);
                confirmaExclusao.setTitle("Atenção!!");
                confirmaExclusao.setMessage("Tem certeza que deseja excluir sua conta: " +
                        nome + "?");
                confirmaExclusao.setPositiveButton("Sim", (dialogInterface, i) -> {
                    for (QueryDocumentSnapshot documento :
                            Objects.requireNonNull(task.getResult())) {
                        ExcluirComentario(documento);
                        DB.collection("usuarios").document(usuarioID)
                                .collection("ordensDeServicos")
                                .document(documento.getId()).delete();
                    }
                    deletarUsuario();
                });
                confirmaExclusao.setNegativeButton("Não", (dialogInterface, i) -> {
                    Intent intent = new Intent(this,
                            TelaConfigConta.class);
                    intent.setFlags(Intent
                            .FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
                confirmaExclusao.setCancelable(false);
                confirmaExclusao.create().show();

            } else {
                Log.w(TAG, "Erro ao deletar documento!");
                Toast.makeText(TelaConfirmacaoExcluirConta.this,
                        "Erro ao deletar conta!", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void deletarUsuario() {
        DB.collection("usuarios").document(usuarioID).delete()
                .addOnSuccessListener(unused -> usuario.delete().addOnCompleteListener(
                        tarefaDoc -> {
                            if (tarefaDoc.isSuccessful()) {
                                Log.d(TAG, "Documento deletado com sucesso!");
                                binding.pBCarregar.setVisibility(View.VISIBLE);
                                binding.btnExcluir.setEnabled(false);
                                new Handler().postDelayed(() -> {
                                    Toast.makeText(TelaConfirmacaoExcluirConta.this,
                                            "Conta deletada com sucesso", Toast.LENGTH_LONG)
                                            .show();
                                    Intent intent = new Intent(this, TelaLogin.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }, 3000);
                            }
                        }))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Erro ao deletar documento!", e);
                    Toast.makeText(this, "Erro ao deletar conta!", Toast.LENGTH_LONG)
                            .show();
                });
    }

    private void ExcluirComentario(@NonNull QueryDocumentSnapshot documento) {
        DB.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .document(documento.getId())
                .collection("comentarios")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(IDCHAT).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentoCom :
                                Objects.requireNonNull(task.getResult())) {
                            DB.collection("usuarios").document(usuarioID)
                                    .collection("ordensDeServicos")
                                    .document(documento.getId())
                                    .collection("comentarios")
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .collection(IDCHAT)
                                    .document(documentoCom.getId()).delete();
                        }
                    }
                });
        DB.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .document(documento.getId())
                .collection("comentarios")
                .document(IDCHAT)
                .collection(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentoCom :
                                Objects.requireNonNull(task.getResult())) {
                            DB.collection("usuarios").document(usuarioID)
                                    .collection("ordensDeServicos")
                                    .document(documento.getId())
                                    .collection("comentarios")
                                    .document(IDCHAT)
                                    .collection(FirebaseAuth.getInstance().getUid())
                                    .document(documentoCom.getId()).delete();
                        }
                    }
                });
    }
}