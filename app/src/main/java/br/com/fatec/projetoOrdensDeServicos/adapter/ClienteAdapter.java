package br.com.fatec.projetoOrdensDeServicos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.databinding.ListaItemClienteBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder> {
    Context context;
    List<Cliente> clientes;
    private final EditarClienteClickListener editarClienteClickListener;
    private final StatusContaClickListener statusContaClickListener;
    private final ListarServicoClickListener listarServicoClickListener;

    public ClienteAdapter(Context context, List<Cliente> clientes,
                          StatusContaClickListener statusContaClickListener,
                          EditarClienteClickListener editarClienteClickListener,
                          ListarServicoClickListener listarServicoClickListener) {
        this.context = context;
        this.clientes = clientes;
        this.editarClienteClickListener = editarClienteClickListener;
        this.statusContaClickListener = statusContaClickListener;
        this.listarServicoClickListener = listarServicoClickListener;
    }

    @NonNull
    @Override
    public ClienteAdapter.ClienteViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        ListaItemClienteBinding binding = ListaItemClienteBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new ClienteViewHolder(binding, statusContaClickListener, editarClienteClickListener,
                listarServicoClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ClienteAdapter.ClienteViewHolder holder,
                                 int position) {
        Drawable backgroundOff = holder.itemView.getBackground();
        holder.itemView.setBackground(backgroundOff);
        Cliente cliente = clientes.get(position);

        holder.txtNome.setText(cliente.getNome().substring(0, 1)
                .toUpperCase().concat(cliente.getNome().substring(1)));
        holder.txtEmail.setText(cliente.getEmail());
        holder.txtTel.setText(cliente.getTelefone());
        holder.txtStatusConta.setText(cliente.getStatusConta());
        TooltipCompat.setTooltipText(holder.imBEditar, Constante.EDITAR_CONTA);
        StatusConta(holder, cliente, backgroundOff);
    }

    private void StatusConta(ClienteViewHolder holder, @NonNull Cliente cliente,
                             Drawable backgroundOff) {
        if (cliente.getStatusConta().equalsIgnoreCase(Constante.BLOQUEADO)) {
            holder.imBStatusConta.setBackgroundResource(R.drawable.lock_icon);
            backgroundOff.setTint(Constante.RED);
            holder.itemView.setBackground(backgroundOff);
            TooltipCompat.setTooltipText(holder.imBStatusConta, Constante.BLOQUEADO);
        } else {
            holder.imBStatusConta.setBackgroundResource(R.drawable.unlock_icon);
            TooltipCompat.setTooltipText(holder.imBStatusConta, Constante.DESBLOQUEADO);
            backgroundOff.setTint(Color.WHITE);
            holder.itemView.setBackground(backgroundOff);
        }
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    public static class ClienteViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView txtNome, txtEmail, txtTel, txtStatusConta;
        ImageButton imBStatusConta, imBEditar;
        StatusContaClickListener statusContaClickListener;
        EditarClienteClickListener editarClienteClickListener;
        ListarServicoClickListener listarServicoClickListener;

        public ClienteViewHolder(@NonNull ListaItemClienteBinding binding,
                                 StatusContaClickListener statusContaClickListener,
                                 EditarClienteClickListener editarClienteClickListener,
                                 ListarServicoClickListener listarServicoClickListener) {
            super(binding.getRoot());
            this.statusContaClickListener = statusContaClickListener;
            this.editarClienteClickListener = editarClienteClickListener;
            this.listarServicoClickListener = listarServicoClickListener;
            txtNome = binding.txtNome;
            txtEmail = binding.txtEmail;
            txtTel = binding.txtTel;
            txtStatusConta = binding.txtStatusConta;
            imBStatusConta = binding.imBStatusConta;
            imBEditar = binding.imBEditar;
            imBStatusConta.setOnClickListener(this::statusConta);
            imBEditar.setOnClickListener(this::editarCliente);
            itemView.setOnClickListener(this);
        }

        public void statusConta(View v){
            statusContaClickListener.StatusContaClick(v, getAbsoluteAdapterPosition());
        }

        public void editarCliente(View v){
            editarClienteClickListener.EditarClienteClick(v, getAbsoluteAdapterPosition());
        }

        @Override
        public void onClick(@NonNull View v) {
            listarServicoClickListener.ListarServicoClick(v, getAbsoluteAdapterPosition());
        }
    }

    public interface EditarClienteClickListener {
        void EditarClienteClick(View v, int position);
    }

    public interface StatusContaClickListener {
        void StatusContaClick(View v, int position);
    }

    public interface ListarServicoClickListener {
        void ListarServicoClick(View v, int position);
    }

}
