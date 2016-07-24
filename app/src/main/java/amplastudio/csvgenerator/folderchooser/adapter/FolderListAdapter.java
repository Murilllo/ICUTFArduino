package amplastudio.csvgenerator.folderchooser.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import amplastudio.csvgenerator.R;

/**
 * Classe que gerencia a lista que mostra todas as pastas em um diretório.
 */
public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.FolderListHolder>{

    public class FolderListHolder extends RecyclerView.ViewHolder{

        public TextView folderName;
        public View itemView;

        public FolderListHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            folderName = (TextView) itemView.findViewById(R.id.activity_folder_chooser_list_row_textview_foldername);
        }

    }

    private String[] foldersName;
    private View.OnClickListener listener;

    public FolderListAdapter(final String[] foldersName){
        this.foldersName = foldersName;
    }

    public void updateFoldersName(final String[] foldersName){
        this.foldersName = foldersName;
        notifyDataSetChanged();
    }

    public void setRecyclerItemClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public FolderListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_folder_chooser_list_row, parent, false);
        return new FolderListHolder(v);
    }

    @Override
    public void onBindViewHolder(FolderListHolder holder, int position) {
        holder.folderName.setText(foldersName[position]);
        holder.itemView.setTag(foldersName[position]);
        if(listener != null) {
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(listener);
        }

    }

    @Override
    public int getItemCount() {
        return foldersName.length;
    }


}
