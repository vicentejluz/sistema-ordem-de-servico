package br.com.fatec.projetoOrdensDeServicos;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
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
import java.util.Locale;
import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityChatBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Comentario;

public class TelaChat extends AppCompatActivity implements View.OnClickListener {
    private String nomeServico, cliente, usuarioID, idServico, privilegio;
    private FirebaseFirestore db;
    String toId, fromId;
    private String eu;
    private GroupieAdapter groupieAdapter;
    private ActivityChatBinding binding;
    private final String IDCHAT = "pb6IdWjCKogMvZlnpH4bl13lCM22AD";
    private final Locale LOCALE = new Locale("pt", "BR");
    private final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            "dd MMMM, yyyy - HH:mm a", LOCALE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        TooltipCompat.setTooltipText(binding.imgBEnviar, "Enviar");
        binding.imgBEnviar.setOnClickListener(this);
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
        Query query = db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", nomeServico);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            idServico = document.getId();
                        }
                    } else {
                        Toast.makeText(this, "Serviços não encontrado",
                                Toast.LENGTH_LONG).show();
                    }
                    db.collection("usuarios").document(Objects.requireNonNull(
                            FirebaseAuth.getInstance().getUid())).get().addOnSuccessListener(
                            documentSnapshot -> {
                                privilegio = documentSnapshot.getString("privilegio");
                                assert privilegio != null;
                                if (privilegio.equalsIgnoreCase("Cliente"))
                                    eu = documentSnapshot.getId();
                                else if (privilegio.equalsIgnoreCase("Admin"))
                                    eu = IDCHAT;
                                else
                                    Log.d("ERROR", "Privilegio não encontrado");
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
                toId = IDCHAT;
            db.collection("usuarios").document(usuarioID)
                    .collection("ordensDeServicos").document(idServico)
                    .collection("comentarios").document(fromId).collection(toId)
                    .orderBy("dataEnvio", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e("Erro no Firestore", error.getMessage());
                            return;
                        }
                        assert value != null;
                        List<DocumentChange> documentChanges = value.getDocumentChanges();

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
        nomeServico = "Nome do serviço não foi setado";
        cliente = "Cliente não foi setado";
        usuarioID = "ID do usuário não foi setado";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nomeServico = extras.getString("nomeServico");
            cliente = extras.getString("cliente");
            usuarioID = extras.getString("usuarioID");

        }
    }

    @Override
    public void onClick(View v) {
        setFromId();
    }

    private void setFromId() {
        db.collection("usuarios").document(Objects.requireNonNull(FirebaseAuth
                .getInstance().getUid())).get()
                .addOnSuccessListener(documentSnapshot -> {
                    privilegio = documentSnapshot.getString("privilegio");
                    assert privilegio != null;
                    if (privilegio.equalsIgnoreCase("Admin"))
                        fromId = IDCHAT;
                    else
                        fromId = FirebaseAuth.getInstance().getUid();
                    procurarIdServico(fromId);
                });
    }

    private void procurarIdServico(String fromId) {
        Query query = db.collection("usuarios").document(usuarioID)
                .collection("ordensDeServicos")
                .whereEqualTo("nomeServico", nomeServico);
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document :
                                Objects.requireNonNull(task.getResult())) {
                            idServico = document.getId();
                        }
                    } else {
                        Toast.makeText(TelaChat.this, "Serviços não encontrado",
                                Toast.LENGTH_LONG).show();
                    }
                    TelaChat.this.sendMessage(fromId);
                });
    }

    private void sendMessage(String fromId) {
        String texto = binding.editTChat.getText().toString().trim();

        binding.editTChat.setText(null);

        assert fromId != null;
        if (!fromId.equals(usuarioID))
            toId = usuarioID;
        else
            toId = IDCHAT;
        Timestamp dataEnvio = Timestamp.now();

        Comentario comentario = new Comentario();
        comentario.setFromId(fromId);
        comentario.setToId(toId);
        comentario.setDataEnvio(dataEnvio);
        comentario.setDescricao(texto);

        if (!comentario.getDescricao().isEmpty()) {
            db.collection("usuarios").document(usuarioID)
                    .collection("ordensDeServicos").document(idServico)
                    .collection("comentarios").document(fromId).collection(toId)
                    .add(comentario)
                    .addOnSuccessListener(documentReference -> Log.d("Ver", documentReference
                            .getId()))
                    .addOnFailureListener(e -> Log.e("Ver1", e.getMessage(), e));
            db.collection("usuarios").document(usuarioID)
                    .collection("ordensDeServicos").document(idServico)
                    .collection("comentarios").document(toId).collection(fromId)
                    .add(comentario)
                    .addOnSuccessListener(documentReference -> Log.d("Ver", documentReference
                            .getId()))
                    .addOnFailureListener(e -> Log.e("Ver1", e.getMessage(), e));
        } else
            Toast.makeText(this, "Não é possível enviar mensagem vazia",
                    Toast.LENGTH_LONG).show();
    }

    private class ItemMensagem extends Item<GroupieViewHolder> {

        private final Comentario comentario;

        private ItemMensagem(Comentario comentario) {
            this.comentario = comentario;
        }


        @SuppressLint("SetTextI18n")
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
                    txtAutor.setText("Suporte");
            }
            txtMsg.setText(comentario.getDescricao());
            txtDataEnvio.setText(SIMPLE_DATE_FORMAT.format(comentario.getDataEnvio().toDate()));
        }

        @Override
        public int getLayout() {
            return comentario.getFromId().equals(eu)
                    ? R.layout.item_to_message : R.layout.item_from_message;
        }

    }
}