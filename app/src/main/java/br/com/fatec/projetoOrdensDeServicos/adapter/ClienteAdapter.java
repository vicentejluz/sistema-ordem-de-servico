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
import br.com.fatec.projetoOrdensDeServicos.entity.Cliente;

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
        View v = LayoutInflater.from(context).inflate(R.layout.lista_item_cliente, parent,
                false);
        return new ClienteViewHolder(v, statusContaClickListener, editarClienteClickListener,
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
        TooltipCompat.setTooltipText(holder.imBEditar, "Editar conta");
        StatusConta(holder, cliente, backgroundOff);
    }

    private void StatusConta(ClienteViewHolder holder, @NonNull Cliente cliente,
                             Drawable backgroundOff) {
        int red = 0x70F43636;
        if (cliente.getStatusConta().equalsIgnoreCase("Bloqueado")) {
            holder.imBStatusConta.setBackgroundResource(R.drawable.lock_icon);
            backgroundOff.setTint(red);
            holder.itemView.setBackground(backgroundOff);
            TooltipCompat.setTooltipText(holder.imBStatusConta, "Bloqueado");
        } else {
            holder.imBStatusConta.setBackgroundResource(R.drawable.unlock_icon);
            TooltipCompat.setTooltipText(holder.imBStatusConta, "Desbloqueado");
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

        public ClienteViewHolder(@NonNull View itemView,
                                 StatusContaClickListener statusContaClickListener,
                                 EditarClienteClickListener editarClienteClickListener,
                                 ListarServicoClickListener listarServicoClickListener) {
            super(itemView);
            this.statusContaClickListener = statusContaClickListener;
            this.editarClienteClickListener = editarClienteClickListener;
            this.listarServicoClickListener = listarServicoClickListener;
            txtNome = itemView.findViewById(R.id.txtNome);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtTel = itemView.findViewById(R.id.txtTel);
            txtStatusConta = itemView.findViewById(R.id.txtStatusConta);
            imBStatusConta = itemView.findViewById(R.id.imBStatusConta);
            imBEditar = itemView.findViewById(R.id.imBEditar);
            imBStatusConta.setOnClickListener(this);
            imBEditar.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(@NonNull View v) {
            if (v.getId() == R.id.imBStatusConta)
                statusContaClickListener.StatusContaClick(v, getAbsoluteAdapterPosition());
            else if (v.getId() == R.id.imBEditar)
                editarClienteClickListener.EditarClienteClick(v, getAbsoluteAdapterPosition());
            else
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
