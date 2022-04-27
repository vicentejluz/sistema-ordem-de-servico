package br.com.fatec.projetoOrdensDeServicos.telaAdmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.databinding.ActivityListarClienteBinding;
import br.com.fatec.projetoOrdensDeServicos.fragment.SelecionarStatusClienteFragment;
import br.com.fatec.projetoOrdensDeServicos.fragment.TodosClientesFragment;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class TelaListarCliente extends AppCompatActivity {
    ActivityListarClienteBinding binding;
    Bundle statusConta = new Bundle();

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
            Fragment selecionarfragmento;
            if (item.getItemId() == R.id.itmTodos)
                selecionarfragmento = new TodosClientesFragment();
            else if (item.getItemId() == R.id.itmDesbloqueado) {
                statusConta.putString(Constante.STATUS_CONTA, Constante.DESBLOQUEADO);
                selecionarfragmento = new SelecionarStatusClienteFragment();
                selecionarfragmento.setArguments(statusConta);
            } else {
                statusConta.putString(Constante.STATUS_CONTA, Constante.BLOQUEADO);
                selecionarfragmento = new SelecionarStatusClienteFragment();
                selecionarfragmento.setArguments(statusConta);
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