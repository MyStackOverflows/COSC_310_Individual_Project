class Product
{
    private int id;
    private String name;
    private int currentStock;
    private double sellPrice;
    private double buyPrice;
    private int shipTimeDays;

    public Product(int id, String name, int currentStock, double sellPrice, double buyPrice, int shipTimeDays)
    {
        this.id = id;
        this.name = name;
        this.currentStock = currentStock;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.shipTimeDays = shipTimeDays;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getCurrentStock()
    {
        return currentStock;
    }

    public void setCurrentStock(int currentStock)
    {
        this.currentStock = currentStock;
    }

    public double getSellPrice()
    {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice)
    {
        this.sellPrice = sellPrice;
    }

    public double getBuyPrice()
    {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice)
    {
        this.buyPrice = buyPrice;
    }

    public int getShipTimeDays()
    {
        return shipTimeDays;
    }

    public void setShipTimeDays(int shipTimeDays)
    {
        this.shipTimeDays = shipTimeDays;
    }

    public String[] toStringArray ()
    {
        return new String[] {
            String.format("%d", id),
            name,
            String.format("%d", currentStock),
            String.format("%.2f", sellPrice),
            String.format("%.2f", buyPrice),
            String.format("%d", shipTimeDays)
        };
    }

    @Override
    public String toString()
    {
        return "Product [id = " + id + ", name = " + name + ", stock = " + currentStock + ", sell price = " + sellPrice + ", buy price = " + buyPrice + ", ship time days = " + shipTimeDays + "]";
    }
}