package br.com.fatec.projetoOrdensDeServicos.telaCliente;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.TelaLogin;

public class TelaMenuCliente extends AppCompatActivity implements View.OnClickListener {
    private TextView txtCliente;
    private final FirebaseFirestore DB = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_cliente);
        txtCliente = findViewById(R.id.txtCliente);
        Button btnCliente = findViewById(R.id.btnCliente);
        Button btnOrdemServico = findViewById(R.id.btnOrdemServico);
        Button btnConsultarServico = findViewById(R.id.btnConsultarServico);
        ImageButton btnSair = findViewById(R.id.btnSair);
        btnSair.setTooltipText("Deslogar");
        btnSair.setOnClickListener(this);
        btnCliente.setOnClickListener(this);
        btnOrdemServico.setOnClickListener(this);
        btnConsultarServico.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(@NonNull View v) {
        switch (v.getId()) {
            case R.id.btnSair:
                telaSair();
                break;
            case R.id.btnCliente:
                telaConfigConta();
                break;
            case R.id.btnOrdemServico:
                telaOrdemDeServico();
                break;
            case R.id.btnConsultarServico:
                telaConsultarServico();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DocumentReference docRef = DB.collection("usuarios").document(usuarioID);
        docRef.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                txtCliente.setText("Usuário: " + documentSnapshot.getString("nome"));
            }
        });

    }

    public void telaConfigConta() {
        Intent intent = new Intent(this, TelaConfigConta.class);
        startActivity(intent);
    }

    public void telaOrdemDeServico() {
        Intent intent = new Intent(this, TelaSolicitarOrcamento.class);
        startActivity(intent);
    }

    public void telaConsultarServico() {
        Intent intent = new Intent(this, TelaConsultarServico.class);
        startActivity(intent);
    }

    public void telaSair() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Conta deslogada com sucesso", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, TelaLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}