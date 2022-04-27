package br.com.fatec.projetoOrdensDeServicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.xwray.groupie.GroupieAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityChatBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Comentario;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaChat extends AppCompatActivity {
    private String nomeServico, cliente, usuarioID, idServico, privilegio;
    private FirebaseFirestore db;
    String toId, fromId;
    private String eu;
    private GroupieAdapter groupieAdapter;
    private ActivityChatBinding binding;
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            Constante.MASCARA_DATA_HORA_BR, Constante.LOCALE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TooltipCompat.setTooltipText(binding.imgBEnviar, Constante.ENVIAR);
        binding.imgBEnviar.setOnClickListener(v -> setFromId());
        db = FirebaseFirestore.getInstance();
        dadosItemLista();
        Objects.requireNonNull(getSupportActionBar()).setTitle(nomeServico.substring(0, 1)
                .toUpperCase().concat(nomeServico.substring(1).toLowerCase()));
        groupieAdapter = new GroupieAdapter();
        binding.rVChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rVChat.setAdapter(groupieAdapter);
        listarMundancaEvento();
        binding.editTChat.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                setFromId();
                handled = true;
            }
            return handled;
        });
        binding.rVChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop,
                                                  oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                binding.rVChat.smoothScrollToPosition(Objects.requireNonNull(binding.rVChat
                        .getAdapter()).getItemCount());
            }
        });

    }

    private void listarMundancaEvento() {
        Query query = db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, nomeServico);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            idServico = document.getId();
                        }
                    } else {
                        Toast.makeText(this, Constante.SERVICO_NAO_ENCONTRADO,
                                Toast.LENGTH_LONG).show();
                    }
                    db.collection(Constante.USUARIOS).document(Objects.requireNonNull(
                            FirebaseAuth.getInstance().getUid())).get().addOnSuccessListener(
                            documentSnapshot -> {
                                privilegio = documentSnapshot.getString(Constante.PRIVILEGIO);
                                if (privilegio != null) {
                                    if (privilegio.equalsIgnoreCase(Constante.CLIENTE))
                                        eu = documentSnapshot.getId();
                                    else if (privilegio.equalsIgnoreCase(Constante.ADMIN))
                                        eu = Constante.IDCHAT;
                                    else
                                        Log.d(Constante.TAG_ERRO, Constante.PRIVILEGIO_NAO_ENCONTRADO);
                                }
                                fetchMessages();
                            });
                });
    }

    private void fetchMessages() {
        String toId;
        if (eu != null) {
            String fromId = eu;
            if (!fromId.equals(usuarioID))
                toId = usuarioID;
            else
                toId = Constante.IDCHAT;
            db.collection(Constante.USUARIOS).document(usuarioID)
                    .collection(Constante.ORDENS_SERVICOS).document(idServico)
                    .collection(Constante.COMENTARIO).document(fromId).collection(toId)
                    .orderBy(Constante.DATA_ENVIO, Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(Constante.TAG_ERRO_FIRESTORE, error.getMessage());
                            return;
                        }
                        List<DocumentChange> documentChanges = Objects.requireNonNull(value)
                                .getDocumentChanges();

                        for (DocumentChange doc : documentChanges) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                Comentario comentario = doc.getDocument().toObject(Comentario.class);
                                groupieAdapter.add(new ItemMensagem(comentario));
                                binding.rVChat.smoothScrollToPosition(groupieAdapter
                                        .getItemCount());
                            }
                        }
                    });


        }
    }

    private void dadosItemLista() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeServico = extras.getString(Constante.NOME_SERVICO);
            cliente = extras.getString(Constante.CLIENTE);
            usuarioID = extras.getString(Constante.USUARIO_ID);

        }
    }

    private void setFromId() {
        db.collection(Constante.USUARIOS).document(Objects.requireNonNull(FirebaseAuth
                .getInstance().getUid())).get()
                .addOnSuccessListener(documentSnapshot -> {
                    privilegio = documentSnapshot.getString(Constante.PRIVILEGIO);
                    if (Objects.requireNonNull(privilegio).equalsIgnoreCase(Constante.ADMIN))
                        fromId = Constante.IDCHAT;
                    else
                        fromId = FirebaseAuth.getInstance().getUid();
                    procurarIdServico(fromId);
                });
    }

    private void procurarIdServico(String fromId) {
        Query query = db.collection(Constante.USUARIOS).document(usuarioID)
                .collection(Constante.ORDENS_SERVICOS)
                .whereEqualTo(Constante.NOME_SERVICO, nomeServico);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            idServico = document.getId();
                        }
                    } else {
                        Toast.makeText(TelaChat.this, Constante.SERVICO_NAO_ENCONTRADO,
                                Toast.LENGTH_LONG).show();
                    }
                    TelaChat.this.sendMessage(fromId);
                });
    }

    private void sendMessage(String fromId) {
        String texto = binding.editTChat.getText().toString().trim();

        binding.editTChat.setText(null);

        if (!fromId.equals(usuarioID))
            toId = usuarioID;
        else
            toId = Constante.IDCHAT;
        Timestamp dataEnvio = Timestamp.now();

        Comentario comentario = new Comentario();
        comentario.setFromId(fromId);
        comentario.setToId(toId);
        comentario.setDataEnvio(dataEnvio);
        comentario.setDescricao(texto);

        if (!comentario.getDescricao().isEmpty()) {
            db.collection(Constante.USUARIOS).document(usuarioID)
                    .collection(Constante.ORDENS_SERVICOS).document(idServico)
                    .collection(Constante.COMENTARIO).document(fromId).collection(toId)
                    .add(comentario)
                    .addOnSuccessListener(documentReference -> Log.d(Constante.TAG_SUCESSO,
                            documentReference.getId()))
                    .addOnFailureListener(e -> Log.e(Constante.TAG_ERRO, e.getMessage(), e));
            db.collection(Constante.USUARIOS).document(usuarioID)
                    .collection(Constante.ORDENS_SERVICOS).document(idServico)
                    .collection(Constante.COMENTARIO).document(toId).collection(fromId)
                    .add(comentario)
                    .addOnSuccessListener(documentReference -> Log.d(Constante.TAG_SUCESSO,
                            documentReference.getId()))
                    .addOnFailureListener(e -> Log.e(Constante.TAG_ERRO, e.getMessage(), e));
        } else
            Toast.makeText(this, Constante.NAO_POSSIVEL_ENVIAR_MSG_VAZIA, Toast.LENGTH_LONG)
                    .show();
    }

    private class ItemMensagem extends Item<GroupieViewHolder> {

        private final Comentario comentario;

        private ItemMensagem(Comentario comentario) {
            this.comentario = comentario;
        }


        @Override
        public void bind(@NonNull GroupieViewHolder groupieViewHolder, int position) {
            TextView txtAutor = groupieViewHolder.itemView.findViewById(R.id.txtAutor);
            TextView txtMsg = groupieViewHolder.itemView.findViewById(R.id.txtMsg);
            TextView txtDataEnvio = groupieViewHolder.itemView.findViewById(R.id.txtdataEnvio);
            if (getLayout() == R.layout.item_from_message) {
                if (cliente != null)
                    txtAutor.setText(cliente.substring(0, 1).toUpperCase().concat(cliente
                            .substring(1).toLowerCase()));
                else
                    txtAutor.setText(Constante.SUPORTE);
            }
            txtMsg.setText(comentario.getDescricao());
            txtDataEnvio.setText(SIMPLE_DATE_FORMAT.format(comentario.getDataEnvio()
                    .toDate()));
        }

        @Override
        public int getLayout() {
            return comentario.getFromId().equals(eu)
                    ? R.layout.item_to_message : R.layout.item_from_message;
        }

    }
}