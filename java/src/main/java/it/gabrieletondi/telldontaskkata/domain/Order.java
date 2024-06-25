package it.gabrieletondi.telldontaskkata.domain;

import it.gabrieletondi.telldontaskkata.useCase.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

public class Order {
    private BigDecimal total = new BigDecimal("0.00");
    private String currency = "EUR";
    private List<OrderItem> items = new ArrayList<>();
    private BigDecimal tax = new BigDecimal("0.00");
    private OrderStatus status = OrderStatus.CREATED;
    private int id;

    public void approveOrReject(boolean approved) {
        if (status.equals(OrderStatus.SHIPPED)) {
            throw new ShippedOrdersCannotBeChangedException();
        }

        if (approved && status.equals(OrderStatus.REJECTED)) {
            throw new RejectedOrderCannotBeApprovedException();
        }

        if (!approved && status.equals(OrderStatus.APPROVED)) {
            throw new ApprovedOrderCannotBeRejectedException();
        }

        this.status = approved ? OrderStatus.APPROVED : OrderStatus.REJECTED;
    }

    public void addItem(SellItemRequest itemRequest, Product product) {
        final BigDecimal unitaryTax = product.getPrice().divide(valueOf(100)).multiply(product.getCategory().getTaxPercentage()).setScale(2, HALF_UP);
        final BigDecimal unitaryTaxedAmount = product.getPrice().add(unitaryTax).setScale(2, HALF_UP);
        final BigDecimal taxedAmount = unitaryTaxedAmount.multiply(BigDecimal.valueOf(itemRequest.getQuantity())).setScale(2, HALF_UP);
        final BigDecimal taxAmount = unitaryTax.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        final OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setTax(taxAmount);
        orderItem.setTaxedAmount(taxedAmount);
        items.add(orderItem);

        this.total = getTotal().add(taxedAmount);
        this.tax = getTax().add(taxAmount);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getCurrency() {
        return currency;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
