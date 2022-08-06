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

import java.util.ArrayList;
import java.util.List;

import br.com.fatec.projetoOrdensDeServicos.databinding.ListaItemServicoBinding;
import br.com.fatec.projetoOrdensDeServicos.entity.OrdemServico;
import br.com.fatec.projetoOrdensDeServicos.util.Constante;

public class OrdemServicoAdapter extends RecyclerView.Adapter<OrdemServicoAdapter
        .OrdemServicoViewHolder> implements Filterable {
    Context context;
    List<OrdemServico> ordemServicos;
    List<OrdemServico> ordemServicosFull;
    private final OrdemServicoClickListener ordemServicoClickListener;
    private final ChatServicoClickListener chatServicoClickListener;

    public OrdemServicoAdapter(Context context, List<OrdemServico> ordemServicos,
                               OrdemServicoClickListener ordemServicoClickListener,
                               ChatServicoClickListener chatServicoClickListener) {
        this.context = context;
        this.ordemServicos = ordemServicos;
        this.ordemServicosFull = new ArrayList<>(ordemServicos);
        this.ordemServicoClickListener = ordemServicoClickListener;
        this.chatServicoClickListener = chatServicoClickListener;
    }

    @NonNull
    @Override
    public OrdemServicoAdapter.OrdemServicoViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                         int viewType) {
        ListaItemServicoBinding binding = ListaItemServicoBinding.inflate(LayoutInflater.from(context),
                parent, false);
        return new OrdemServicoViewHolder(binding, ordemServicoClickListener, chatServicoClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdemServicoAdapter.OrdemServicoViewHolder holder,
                                 int position) {
        OrdemServico ordemServico = ordemServicos.get(position);

        holder.txtNomeServico.setText(ordemServico.getNomeServico().substring(0, 1)
                .toUpperCase().concat(ordemServico.getNomeServico().substring(1)));
        holder.txtStatus.setText(ordemServico.getStatus().name().substring(0, 1)
                .concat(ordemServico.getStatus().name().substring(1).toLowerCase()));
        TooltipCompat.setTooltipText(holder.imBChat, Constante.CHAT_SUPORTE);
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

    public static class OrdemServicoViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView txtNomeServico, txtStatus;
        ImageButton imBChat;
        OrdemServicoClickListener ordemServicoClickListener;
        ChatServicoClickListener chatServicoClickListener;

        public OrdemServicoViewHolder(@NonNull ListaItemServicoBinding binding,
                                      OrdemServicoClickListener ordemServicoClickListener,
                                      ChatServicoClickListener chatServicoClickListener) {
            super(binding.getRoot());
            txtNomeServico = binding.txtNomeServico;
            txtStatus = binding.txtStatus;
            imBChat = binding.imBChat;
            this.ordemServicoClickListener = ordemServicoClickListener;
            this.chatServicoClickListener = chatServicoClickListener;
            imBChat.setOnClickListener(this::chatServico);
            binding.getRoot().setOnClickListener(this);
        }

        public void chatServico(View v) {
            chatServicoClickListener.ChatServicoClick(v, getAbsoluteAdapterPosition());
        }

        @Override
        public void onClick(View v) {
            ordemServicoClickListener.OrdemServicoClick(v, getAbsoluteAdapterPosition());
        }
    }

    public interface OrdemServicoClickListener {
        void OrdemServicoClick(View v, int position);
    }

    public interface ChatServicoClickListener {
        void ChatServicoClick(View v, int position);
    }
}
