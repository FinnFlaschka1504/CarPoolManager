package finn_daniel.carpoolmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRecycler {
    private Context context;
    private RecyclerView recycler;
    private int itemView;
    private List<Integer> viewIdList;
    private SetCellContent setItemContent;
    private List objectList;

    public static CustomRecycler Builder(Context context) {
        return new CustomRecycler(context);
    }
    public static CustomRecycler Builder(Context context, RecyclerView recycler) {
        CustomRecycler customRecycler = new CustomRecycler(context);
        customRecycler.recycler = recycler;
        return customRecycler;
    }

    // ToDo: eventuell per z.B. User.class die dataset-Liste nicht abstrakt machen

    private CustomRecycler(Context context) {
    }

    public CustomRecycler setItemView(int layoutId) {
        this.itemView = layoutId;
        return this;
    }

    public CustomRecycler setObjectList(List objectList) {
        this.objectList = objectList;
        return this;
    }

    public interface SetViewList{
        List<Integer> runSetViewList(List<Integer> viewList);
    }

    public CustomRecycler setViewList(SetViewList viewList) {
        this.viewIdList = viewList.runSetViewList(new ArrayList<>());
        return this;
    }

    public interface SetCellContent {
        void runSetCellContent(Map<Integer, View> integerViewMap, Object o);
    }

    public CustomRecycler setSetItemContent(SetCellContent setItemContent) {
        this.setItemContent = setItemContent;
        return this;
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List dataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        // Provide a suitable constructor (depends on the kind of dataset)

        public MyAdapter(List list) {
            dataset = list;

//            ((User) list.get(0))
        }
        // Create new views (invoked by the layout manager)

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(itemView, parent, false);
            return new ViewHolder(v);
        }
        // Replace the contents of a view (invoked by the layout manager)

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
//            holder.textView.setText(dataset[position]);
//            holder.viewMap.get()
            setItemContent.runSetCellContent(holder.viewMap, dataset.get(position));
        }
        // Return the size of your dataset (invoked by the layout manager)

        @Override
        public int getItemCount() {
            return dataset.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
//            public TextView textView;
            Map<Integer, View> viewMap = new HashMap<>();

            public ViewHolder(View v) {
                super(v);
                for (Integer id : viewIdList) {
                    viewMap.put(id, v.findViewById(id));
                }
            }
        }
    }

    public RecyclerView generate() {
        RecyclerView recyclerView = new RecyclerView(context);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        RecyclerView.Adapter mAdapter = new MyAdapter(objectList);
        recyclerView.setAdapter(mAdapter);

        return recyclerView;
    }

}
