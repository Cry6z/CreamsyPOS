package com.example.creamsy;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    public interface ProductActionListener {
        void onAddToCart(Product product);
        void onEdit(Product product);
        void onDelete(Product product);
    }

    private final Context context;
    private List<Product> products;
    private final ProductActionListener listener;

    public ProductAdapter(Context context, List<Product> products, ProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    public void updateData(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getNama());
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        holder.tvPrice.setText(nf.format(product.getHarga()));
        holder.tvStock.setText("Stok: " + product.getStok());

        if (product.getFotoUri() != null && !product.getFotoUri().isEmpty()) {
            try {
                holder.imgProduct.setImageURI(Uri.parse(product.getFotoUri()));
            } catch (Exception e) {
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(product));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvStock;
        Button btnAddToCart, btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvProductStock);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
