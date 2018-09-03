package lab1a;

public class GuessTheWordServer {

    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("You need to enter a word.");
            System.out.println("Usage: java guessthewordserver [word]");
            System.exit(1);
        }

        System.out.println("Word set to: " + args[0]);
    }
}
