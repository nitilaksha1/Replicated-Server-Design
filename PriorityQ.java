// Test.java
import java.util.Comparator;
import java.util.PriorityQueue;

class TimeStamp {

    public int timestamp;
    public int serverid;
}

public class Test
{
    public static void main(String[] args)
    {
        Comparator<TimeStamp> comparator = new TimeStampComparator();
        PriorityQueue<TimeStamp> queue = 
            new PriorityQueue<TimeStamp>(10, comparator);

        queue.add(new TimeStamp(1, 1));
        queue.add(new TimeStamp(1, 2));
        queue.add(new TimeStamp(2, 1));

        while (queue.size() != 0)
        {
            System.out.println(queue.remove());
        }
    }
}

// StringLengthComparator.java
import java.util.Comparator;

public class TimeStampComparator implements Comparator<TimeStamp>
{
    @Override
    public int compare(TimeStamp x, TimeStamp y)
    {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.timestamp < y.timestamp)
        {
            res = -1;
        }
        else if (x.timestamp > y.timestamp)
        {
            res = 1;
        } 
        else if (x.timestamp == y.timestamp) {
            if (x.serverid > y.serverid)
                res = 1;
            else
                res = -1;
        }

        return res;
    }
}