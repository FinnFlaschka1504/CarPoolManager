package finn_daniel.carpoolmanager;

import android.content.Context;
import android.util.TypedValue;
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
    enum ORIENTATION {
        VERTICAL, HORIZONTAL
    }

    private boolean useCustomRipple = false;
    private Context context;
    private RecyclerView recycler;
    private int itemView;
    private List<Integer> viewIdList;
    private SetCellContent setItemContent;
    private List objectList;
    private int orientation = LinearLayoutManager.HORIZONTAL;
    private OnLongClickListener onLongClickListener;
    private View.OnLongClickListener longClickListener = view -> {
        int index = ((ViewGroup) view.getParent()).indexOfChild(view);
        onLongClickListener.runOnLongClickListener(view, objectList.get(index), index);
        return true;
    };
    private OnClickListener onClickListener;
    private View.OnClickListener clickListener = view -> {
        int index = ((ViewGroup) view.getParent()).indexOfChild(view);
        onClickListener.runOnClickListener(view, objectList.get(index), index);
    };

    private CustomRecycler(Context context) {
        this.context = context;
    }

    public static CustomRecycler Builder(Context context) {
        return new CustomRecycler(context);
    }

    public static CustomRecycler Builder(Context context, RecyclerView recycler) {
        CustomRecycler customRecycler = new CustomRecycler(context);
        customRecycler.recycler = recycler;
        return customRecycler;
    }

    // ToDo: eventuell per z.B. User.class die dataset-Liste nicht abstrakt machen

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

    public CustomRecycler setOrientation(ORIENTATION orientation) {
        switch (orientation) {
            case VERTICAL: this.orientation = LinearLayoutManager.VERTICAL; break;
            case HORIZONTAL: this.orientation = LinearLayoutManager.HORIZONTAL; break;
        }
        return this;
    }

    public CustomRecycler setUseCustomRipple(boolean useCustomRipple) {
        this.useCustomRipple = useCustomRipple;
        return this;
    }

    public interface SetCellContent {
        void runSetCellContent(Map<Integer, View> integerViewMap, Object object);
    }

    public CustomRecycler setSetItemContent(SetCellContent setItemContent) {
        this.setItemContent = setItemContent;
        return this;
    }

    public interface OnClickListener {

        void runOnClickListener(View view, Object object, int index);
    }

    public CustomRecycler setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public interface OnLongClickListener {
        void runOnLongClickListener(View view, Object object, int index);
    }

    public CustomRecycler setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private List dataset;

        public MyAdapter(List list) {
            dataset = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(itemView, parent, false);
            if (onClickListener != null) {
                v.setOnClickListener(clickListener);
                if (!useCustomRipple) {
                    v.setFocusable(true);
                    v.setClickable(true);
                    TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    v.setBackgroundResource(outValue.resourceId);
                }
            }

            if (onLongClickListener != null)
                v.setOnLongClickListener(longClickListener);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            setItemContent.runSetCellContent(holder.viewMap, dataset.get(position));
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
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
        RecyclerView recyclerView;
        if (this.recycler == null)
            recyclerView = new RecyclerView(context);
        else
            recyclerView = recycler;

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, orientation, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter mAdapter = new MyAdapter(objectList);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setClickable(true);

        return recyclerView;
    }

}
