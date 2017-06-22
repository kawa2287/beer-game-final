package mkawa.okhttp;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Color;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.vstechlab.easyfonts.EasyFonts;

import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;

public class TokenGraphAdapter extends RecyclerView.Adapter<TokenGraphAdapter.TGViewHolder>
{

    private List<TokenGraph> tokenGraphList;
    private ClickListener clickListener;

    public class TGViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView tokenIcon;
        public AutofitTextView tokenPurseValTV;
        public AutofitTextView tokenCurValTV;
        public AutofitTextView tokenBaseValTV;
        public CombinedChart tokenGraph;
        public LinearLayout mainLay;
        public LinearLayout tokenTitle;

        public TGViewHolder(View view)
        {
            super(view);
            tokenIcon = (ImageView) view.findViewById(R.id.tokenIcon);
            tokenPurseValTV = (AutofitTextView) view.findViewById(R.id.tokenPurseCount);
            tokenCurValTV = (AutofitTextView) view.findViewById(R.id.tokenCurVal);
            tokenBaseValTV = (AutofitTextView) view.findViewById(R.id.tokenBaseVal);
            tokenGraph = (CombinedChart) view.findViewById(R.id.tokenGraph);
            mainLay = (LinearLayout) view.findViewById(R.id.tokenLayout);
            tokenTitle = (LinearLayout) view.findViewById(R.id.tokenTitle);

            mainLay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(clickListener != null)
                    {
                        clickListener.itemClicked(v, getAdapterPosition());
                    }
                }
            });
        }
    }


    public TokenGraphAdapter(List<TokenGraph> tokenGraphList)
    {
        this.tokenGraphList = tokenGraphList;
    }

    @Override
    public TGViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.token_market, parent, false);

        return new TGViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TGViewHolder holder, int position){
        TokenGraph tokenGraph = tokenGraphList.get(position);

        holder.tokenIcon.setImageResource(tokenGraph.getTokenID());
        holder.tokenCurValTV.setText("CV : " + String.valueOf(tokenGraph.getCurVal()));
        holder.tokenCurValTV.setTypeface(EasyFonts.ostrichRegular(holder.tokenGraph.getContext()));
        holder.tokenBaseValTV.setText("BV : " + String.valueOf(tokenGraph.getBaseVal()));
        holder.tokenBaseValTV.setTypeface(EasyFonts.ostrichRegular(holder.tokenGraph.getContext()));
        holder.tokenPurseValTV.setTextColor(ContextCompat.getColor(holder.tokenGraph.getContext(), R.color.Gold));
        holder.tokenPurseValTV.setTypeface(EasyFonts.ostrichBlack(holder.tokenGraph.getContext()));
        holder.tokenPurseValTV.setText("SH : " + String.valueOf(tokenGraph.getTokenPurseVal()));


        //SET GRAPH FORMATTING
        XAxis xAxis = holder.tokenGraph.getXAxis();
        xAxis.setAxisMaxValue(7f);
        xAxis.setAxisMinValue(0f);
        xAxis.setDrawLimitLinesBehindData(true);
        xAxis.setDrawLabels(false);
        xAxis.setGridColor(ContextCompat.getColor(holder.tokenGraph.getContext(), R.color.DarkGoldenrod));

        holder.tokenGraph.setDescription("");
        holder.tokenGraph.setDrawBorders(true);
        holder.tokenGraph.setBorderColor(ContextCompat.getColor(holder.tokenGraph.getContext(), R.color.DarkGoldenrod));
        holder.tokenGraph.setTouchEnabled(false);
        holder.tokenGraph.getLegend().setEnabled(false);

        //SET GRAPH DATA FOR STOCK (LINE GRAPH)
        ArrayList<Float> stockValues = new ArrayList<>();
        ArrayList<Entry> graphEntries = new ArrayList<>();
            graphEntries.add(new Entry(1f, tokenGraph.getVal5()));
            graphEntries.add(new Entry(2f, tokenGraph.getVal4()));
            graphEntries.add(new Entry(3f, tokenGraph.getVal3()));
            graphEntries.add(new Entry(4f, tokenGraph.getVal2()));
            graphEntries.add(new Entry(5f, tokenGraph.getVal1()));
            graphEntries.add(new Entry(6f, tokenGraph.getCurVal()));
            stockValues.add(tokenGraph.getVal5());
            stockValues.add(tokenGraph.getVal4());
            stockValues.add(tokenGraph.getVal3());
            stockValues.add(tokenGraph.getVal2());
            stockValues.add(tokenGraph.getVal1());
            stockValues.add(tokenGraph.getCurVal());

        LineDataSet marketDataSet = new LineDataSet(graphEntries, "time");
        marketDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        marketDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        List<ILineDataSet> stockDataSets = new ArrayList<>();
        stockDataSets.add(marketDataSet);
        LineData stockData = new LineData(stockDataSets);

        //SET GRAPH DATA FOR BASE VALUES (BAR GRAPH)
        ArrayList<Float> baseValues = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
            barEntries.add(new BarEntry(1f, tokenGraph.getBv5()));
            barEntries.add(new BarEntry(2f, tokenGraph.getBv4()));
            barEntries.add(new BarEntry(3f, tokenGraph.getBv3()));
            barEntries.add(new BarEntry(4f, tokenGraph.getBv2()));
            barEntries.add(new BarEntry(5f, tokenGraph.getBv1()));
            barEntries.add(new BarEntry(6f, tokenGraph.getBaseVal()));
            baseValues.add(tokenGraph.getBv5());
            baseValues.add(tokenGraph.getBv4());
            baseValues.add(tokenGraph.getBv3());
            baseValues.add(tokenGraph.getBv2());
            baseValues.add(tokenGraph.getBv1());
            baseValues.add(tokenGraph.getBaseVal());

        //SET LINE GRAPH RANGE
        float deltaStockVal = 0;
        for (int i = 0; i < baseValues.size(); i++)
        {
            float testDelta = Math.abs(stockValues.get(i) - tokenGraph.getBaseVal());
            if (testDelta > deltaStockVal)
            {
                deltaStockVal = testDelta;
            }
        }

        //SET BAR GRAPH RANGE
        float deltaBaseVal = 0;
        for (int i = 0; i < baseValues.size(); i++)
        {
            float testDelta = Math.abs(baseValues.get(i) - tokenGraph.getBaseVal());
            if (testDelta > deltaBaseVal)
            {
                deltaBaseVal = testDelta;
            }
        }

        //Right Axis = LINEGRAPH AXIS
        YAxis rightAxis = holder.tokenGraph.getAxisRight();
        rightAxis.setGranularityEnabled(true);
        rightAxis.setGranularity(0.1f);
        rightAxis.setLabelCount(5, true);
        rightAxis.setAxisMinValue(0f);
        rightAxis.setAxisMaxValue(tokenGraph.getMaxStockVal() + 0.2f);
        rightAxis.setSpaceBottom(100f);
        rightAxis.setSpaceTop(100f);
        rightAxis.setDrawLabels(true);
        rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        rightAxis.setTextColor(ContextCompat.getColor(holder.tokenGraph.getContext(), R.color.GhostWhite));
        rightAxis.setTypeface(EasyFonts.ostrichBlack(holder.tokenGraph.getContext()));

        //Left Axis = BARCHART AXIS
        YAxis leftAxis = holder.tokenGraph.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.1f);
        leftAxis.setLabelCount(5, true);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(tokenGraph.getMaxStockVal() + 0.2f);
        leftAxis.setSpaceBottom(100f);
        leftAxis.setSpaceTop(100f);


        BarDataSet baseValDataSet = new BarDataSet(barEntries, "time");
        baseValDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        baseValDataSet.setColor(ContextCompat.getColor(holder.tokenGraph.getContext(),R.color.DarkGoldenrod));
        baseValDataSet.setDrawValues(false);
        List<IBarDataSet> bvDataSets = new ArrayList<>();
        bvDataSets.add(baseValDataSet);
        BarData bvData = new BarData(bvDataSets);
        bvData.setBarWidth(0.5f);

        //COMPILE DATA INTO COMBINED FORMAT
        CombinedData combinedData = new CombinedData();
        combinedData.setData(bvData);
        combinedData.setData(stockData);

        //SET VIEW AND CLICK BOOLEAN BASED ON IF USER HAS ENOUGH TO PURCHASE
        if(tokenGraph.getHaveToken() == null)
        {
            holder.mainLay.setClickable(false);
            holder.tokenIcon.setColorFilter(Color.argb(250, 0, 0, 0));
            holder.tokenGraph.setNoDataText("");
        }
        else if(tokenGraph.getHaveToken().equals(true))
        {
            holder.mainLay.setClickable(false);
            holder.tokenIcon.setColorFilter(Color.argb(0, 0, 0, 0));
            holder.tokenGraph.setData(combinedData);
            holder.tokenGraph.getLineData().setDrawValues(false);
            holder.tokenGraph.setViewPortOffsets(0f,0f,0f,0f);
            holder.tokenGraph.animateY(1000);
        }
        else
        {
            holder.mainLay.setClickable(false);
            holder.tokenIcon.setColorFilter(Color.argb(250, 0, 0, 0));
            holder.tokenGraph.setNoDataText("");
        }


        if(tokenGraph.getCurVal()*tokenGraph.getTokenPurseVal() >= 1)
        {
            holder.mainLay.setClickable(true);
            holder.tokenTitle.setBackgroundResource(R.drawable.token_purchase_title);
        }
        else
        {
            holder.mainLay.setClickable(false);
            holder.tokenTitle.setBackgroundResource(R.drawable.token_title);
        }
    }

    public void setClickListener (ClickListener clickListener)
    {
        this.clickListener = clickListener;

    }

    @Override
    public int getItemCount(){
        return tokenGraphList.size();
    }

    public interface ClickListener
    {
        void itemClicked(View view, int position);
    }


}


