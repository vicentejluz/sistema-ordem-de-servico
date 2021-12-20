package br.com.fatec.projetoOrdensDeServicos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class TelaAbertura extends AppCompatActivity {
    private final Timer TIMER = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_abertura);

        final int TEMPO_ABERTURA = 3000;
        TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                Intent intent = new Intent(TelaAbertura.this, TelaLogin.class);
                startActivity(intent);
                finish();
            }
        }, TEMPO_ABERTURA);
    }
}