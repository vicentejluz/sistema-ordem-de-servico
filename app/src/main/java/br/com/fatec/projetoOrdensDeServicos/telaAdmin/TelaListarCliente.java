package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityListarClienteBinding;
import br.com.fatec.projetoOrdensDeServicos.fragment.SelecionarStatusClienteFragment;
import br.com.fatec.projetoOrdensDeServicos.fragment.TodosClientesFragment;

public class TelaListarCliente extends AppCompatActivity {
    ActivityListarClienteBinding binding;
    Bundle statusConta = new Bundle();

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListarClienteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (savedInstanceState == null) {
            LoadFragment(new TodosClientesFragment());
        }

        binding.bNVStatusConta.setOnItemSelectedListener(item -> {
            Fragment selecionarfragmento = null;
            switch (item.getItemId()) {
                case R.id.itmTodos:
                    selecionarfragmento = new TodosClientesFragment();
                    break;
                case R.id.itmDesbloqueado:
                    statusConta.putString("statusConta", "Desbloqueado");
                    selecionarfragmento = new SelecionarStatusClienteFragment();
                    selecionarfragmento.setArguments(statusConta);
                    break;
                case R.id.itmBloqueado:
                    statusConta.putString("statusConta", "Bloqueado");
                    selecionarfragmento = new SelecionarStatusClienteFragment();
                    selecionarfragmento.setArguments(statusConta);
                    break;
            }
            return LoadFragment(selecionarfragmento);
        });
    }

    private boolean LoadFragment(Fragment selecionarfragmento) {
        if (selecionarfragmento != null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fragment_container_recyclerview, selecionarfragmento).commit();
            return true;
        }
        return false;
    }
}