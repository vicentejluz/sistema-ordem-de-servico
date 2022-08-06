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

import br.com.fatec.projetoOrdensDeServicos.R;
import br.com.fatec.projetoOrdensDeServicos.databinding.ListaItemServicoAdminBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.entity.StatusOrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class OrdemServicoAdminAdapter extends RecyclerView.Adapter<OrdemServicoAdminAdapter
        .OrdemServicoAdminViewHolder> implements Filterable {
    Context context;
    List<OrdemServico> ordemServicos;
    List<OrdemServico> ordemServicosFull;
    List<String> clientes;
    String cliente;
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
        ListaItemServicoAdminBinding binding = ListaItemServicoAdminBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new OrdemServicoAdminViewHolder(binding, ordemServicoAdminClickListener,
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
        TooltipCompat.setTooltipText(holder.imBExcluir, Constante.EXCLUIR_SERVICO);
        holder.txtNomeServico.setText(ordemServico.getNomeServico().substring(0, 1).toUpperCase()
                .concat(ordemServico.getNomeServico().substring(1)));
        if(ordemServico.getPreco() != null)
            holder.txtPreco.setText(NumberFormat.getCurrencyInstance(Constante.LOCALE).format(
                    ordemServico.getPreco()));
        else
            holder.txtPreco.setText(Constante.SEM_PRECO);
        holder.txtStatus.setText(ordemServico.getStatus().name().substring(0, 1).concat(
                ordemServico.getStatus().name().substring(1).toLowerCase()));
        TooltipCompat.setTooltipText(holder.imBChat, Constante.CHAT);
        restricoes(holder, ordemServico);
    }

    private void restricoes(OrdemServicoAdminViewHolder holder,
                            @NonNull OrdemServico ordemServico) {
        if (!ordemServico.getStatus().name().equalsIgnoreCase(StatusOrdemServico.FINALIZADA.name())) {
            holder.imBExcluir.setBackgroundResource(R.drawable.industry_trash_icon);
            holder.imBExcluir.setEnabled(false);
        } else
            holder.imBExcluir.setBackgroundResource(R.drawable.user_trash_full_icon);
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
        TextView txtNomeServico, txtStatus, txtCliente, txtPreco, tVCliente;
        ImageButton imBExcluir, imBChat;
        OrdemServicoAdminClickListener ordemServicoAdminClickListener;
        ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener;
        ChatServicoClickListener chatServicoClickListener;

        public OrdemServicoAdminViewHolder(@NonNull ListaItemServicoAdminBinding binding,
                                           OrdemServicoAdminClickListener ordemServicoAdminClickListener,
                                           ExcluirOrdemServicoClickListener excluirOrdemServicoClickListener,
                                           ChatServicoClickListener chatServicoClickListener) {
            super(binding.getRoot());
            txtNomeServico = binding.txtNomeServico;
            txtStatus = binding.txtStatus;
            txtCliente = binding.txtCliente;
            txtPreco = binding.txtPreco;
            tVCliente = binding.tVCliente;
            imBExcluir = binding.imBExcluir;
            imBChat = binding.imBChat;
            imBExcluir.setOnClickListener(this::excluirSevico);
            imBChat.setOnClickListener(this::chatServico);
            this.ordemServicoAdminClickListener = ordemServicoAdminClickListener;
            this.excluirOrdemServicoClickListener = excluirOrdemServicoClickListener;
            this.chatServicoClickListener = chatServicoClickListener;
            binding.getRoot().setOnClickListener(this);
        }

        public void excluirSevico(View v){
            excluirOrdemServicoClickListener.ExcluirOrdemServicoClick(v, getAbsoluteAdapterPosition());
        }

        public void chatServico(View v){
            chatServicoClickListener.ChatServicoClick(v, getAbsoluteAdapterPosition());
        }

        @Override
        public void onClick(View v) {
            ordemServicoAdminClickListener.OrdemServicoAdminClick(v, getAbsoluteAdapterPosition());
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