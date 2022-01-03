package br.com.fatec.projetoOrdensDeServicos.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;

public class OrdemServicoAdminAdapter extends RecyclerView.Adapter<OrdemServicoAdminAdapter
        .OrdemServicoAdminViewHolder> implements Filterable {
    Context context;
    List<OrdemServico> ordemServicos;
    List<OrdemServico> ordemServicosFull;
    List<String> clientes;
    String cliente;
    private final Locale LOCALE = new Locale("pt", "BR");
    private final OrdemServicoAdminClickListener ordemServicoAdminClickListener;
    private final ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener;
    private final ChatServicoClickListener chatServicoClickListener;

    public OrdemServicoAdminAdapter(Context context, List<OrdemServico> ordemServicos,
                                    List<String> clientes,
                                    OrdemServicoAdminClickListener ordemServicoAdminClickListener,
                                    ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener,
                                    ChatServicoClickListener chatServicoClickListener) {
        this.context = context;
        this.ordemServicos = ordemServicos;
        this.clientes = clientes;
        this.ordemServicosFull = new ArrayList<>(ordemServicos);
        this.ordemServicoAdminClickListener = ordemServicoAdminClickListener;
        this.excluirOrdemServicoClickListener = excluirOrdemServicoClickListener;
        this.chatServicoClickListener = chatServicoClickListener;
    }

    @NonNull
    @Override
    public OrdemServicoAdminAdapter.OrdemServicoAdminViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.lista_item_servico_admin, parent,
                false);
        return new OrdemServicoAdminViewHolder(v, ordemServicoAdminClickListener,
                excluirOrdemServicoClickListener, chatServicoClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdemServicoAdminAdapter.OrdemServicoAdminViewHolder holder,
                                 int position) {
        OrdemServico ordemServico = ordemServicos.get(position);
        if (!clientes.isEmpty()) {
            cliente = clientes.get(position);
            holder.txtCliente.setText(cliente.substring(0, 1).toUpperCase().concat(
                    cliente.substring(1).toLowerCase()));
        } else {
            holder.txtCliente.setVisibility(View.GONE);
            holder.tVCliente.setVisibility(View.GONE);
        }

        holder.imBExcluir.setEnabled(true);
        TooltipCompat.setTooltipText(holder.imBExcluir, "Excluir Serviço");
        holder.txtNomeServico.setText(ordemServico.getNomeServico().substring(0, 1)
                .toUpperCase().concat(ordemServico.getNomeServico().substring(1)));
        holder.txtDescricao.setText(ordemServico.getDescricao().substring(0, 1)
                .toUpperCase().concat(ordemServico.getDescricao().substring(1)));
        holder.txtPreco.setText(NumberFormat.getCurrencyInstance(LOCALE).format(ordemServico
                .getPreco()));
        holder.txtStatus.setText(ordemServico.getStatus().substring(0, 1)
                .concat(ordemServico.getStatus().substring(1).toLowerCase()));
        TooltipCompat.setTooltipText(holder.imBChat, "Chat");
        restricoes(holder, ordemServico);
    }

    @SuppressLint("SetTextI18n")
    private void restricoes(OrdemServicoAdminViewHolder holder,
                            @NonNull OrdemServico ordemServico) {
        if (!ordemServico.getStatus().equalsIgnoreCase("FINALIZADA")) {
            holder.imBExcluir.setBackgroundResource(R.drawable.industry_trash_icon);
            holder.imBExcluir.setEnabled(false);
        } else
            holder.imBExcluir.setBackgroundResource(R.drawable.user_trash_full_icon);

        if (ordemServico.getPreco() == 0.0)
            holder.txtPreco.setText("Sem preço");
    }

    @Override
    public int getItemCount() {
        return ordemServicos.size();
    }

    @Override
    public Filter getFilter() {
        return novoFiltro;
    }

    private final Filter novoFiltro = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<OrdemServico> filtroNovaLista = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filtroNovaLista.addAll(ordemServicosFull);
            } else {
                String filtroPadrao = constraint.toString().toLowerCase().trim();

                for (OrdemServico os : ordemServicosFull) {
                    if (os.getNomeServico().toLowerCase().contains(filtroPadrao))
                        filtroNovaLista.add(os);
                }
            }
            FilterResults resultados = new FilterResults();
            resultados.values = filtroNovaLista;
            resultados.count = filtroNovaLista.size();
            return resultados;
        }

        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {

            ordemServicos.clear();
            ordemServicos.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class OrdemServicoAdminViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView txtNomeServico, txtDescricao, txtStatus, txtCliente, txtPreco, tVCliente;
        ImageButton imBExcluir, imBChat;
        OrdemServicoAdminClickListener ordemServicoAdminClickListener;
        ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener;
        ChatServicoClickListener chatServicoClickListener;

        public OrdemServicoAdminViewHolder(@NonNull View itemView, OrdemServicoAdminClickListener
                ordemServicoAdminClickListener, ExcluirOrdemServicoClickListener
                excluirOrdemServicoClickListener, ChatServicoClickListener
                chatServicoClickListener) {
            super(itemView);
            txtNomeServico = itemView.findViewById(R.id.txtNomeServico);
            txtDescricao = itemView.findViewById(R.id.txtDescricao);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtCliente = itemView.findViewById(R.id.txtCliente);
            txtPreco = itemView.findViewById(R.id.txtPreco);
            tVCliente = itemView.findViewById(R.id.tVCliente);
            imBExcluir = itemView.findViewById(R.id.imBExcluir);
            imBChat = itemView.findViewById(R.id.imBChat);
            imBExcluir.setOnClickListener(this);
            imBChat.setOnClickListener(this);
            this.ordemServicoAdminClickListener = ordemServicoAdminClickListener;
            this.excluirOrdemServicoClickListener = excluirOrdemServicoClickListener;
            this.chatServicoClickListener = chatServicoClickListener;
            itemView.setOnClickListener(this);
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.imBExcluir)
                excluirOrdemServicoClickListener.ExcluirOrdemServicoClick(v,
                        getAbsoluteAdapterPosition());
            else if (v.getId() == R.id.imBChat)
                chatServicoClickListener.ChatServicoClick(v, getAbsoluteAdapterPosition());
            else
                ordemServicoAdminClickListener.OrdemServicoAdminClick(v,
                        getAbsoluteAdapterPosition());
        }
    }

    public interface OrdemServicoAdminClickListener {
        void OrdemServicoAdminClick(View v, int position);
    }

    public interface ExcluirOrdemServicoClickListener {
        void ExcluirOrdemServicoClick(View v, int position);
    }

    public interface ChatServicoClickListener {
        void ChatServicoClick(View v, int position);
    }
}