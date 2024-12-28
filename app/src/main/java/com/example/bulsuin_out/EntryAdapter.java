package com.example.bulsuin_out;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private List<Entry> entryList;

    public EntryAdapter(List<Entry> entryList) {
        this.entryList = entryList;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        Entry entry = entryList.get(position);
        holder.nametxt.setText(entry.getName());
        holder.roletxt.setText(entry.getRole());
        holder.departmenttxt.setText(entry.getDepartmentOrPurpose());
        holder.modeltxt.setText(entry.getVehicleModel());
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView nametxt, roletxt, departmenttxt, modeltxt;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            nametxt = itemView.findViewById(R.id.nametxt);
            roletxt = itemView.findViewById(R.id.roletxt);
            departmenttxt = itemView.findViewById(R.id.departmenttxt);
            modeltxt = itemView.findViewById(R.id.platetxt);
        }
    }
}
