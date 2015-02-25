package github.luv.mockgeofix;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import github.luv.mockgeofix.command.HelpCommand;
import github.luv.mockgeofix.command.PasswordCommand;
import github.luv.mockgeofix.util.ResponseWriter;

public class CommandDispatcher {
    String TAG = "CommandDispatcher";

    static private CommandDispatcher instance = new CommandDispatcher();
    static public CommandDispatcher getInstance() { return instance; }
    private CommandDispatcher() {}

    protected Context mContext;
    protected PasswordCommand passwordCommand;
    protected HelpCommand helpCommand;

    static public void init(Context context) {
        getInstance()._init(context);
    }

    static public void dispatch(SocketChannel client, String command) {
        getInstance()._verifyInitiated();
        getInstance()._dispatch(client, command);
    }

    protected void _init(Context context) {
        if (mContext != null) {
            throw new AssertionError("CommandDispatcher.init called twice!");
        }
        mContext = context;
        passwordCommand = new PasswordCommand(context);
        helpCommand = new HelpCommand();
    }

    protected void _dispatch(SocketChannel client, String command) {
        String cmd = command.split(" ", 2)[0].toLowerCase();
        if (cmd.equals("password")) {
            passwordCommand.execute(client, command);
        } else if (cmd.equals("geo")) {
            if ( passwordCommand.passwordRequired() && (! passwordCommand.loggedIn(client)) ) {
                ResponseWriter.notLoggedIn(client);
            } else {
                Log.i(TAG, "GEO FIX::" + command);
            }
        } else if (cmd.equals("help")) {
            helpCommand.execute(client, command);
        } else if (cmd.equals("quit") || cmd.equals("exit")) {
            try { client.close(); } catch(IOException ignored) {}
        } else {
            ResponseWriter.unknownCommand(client);
        }
    }

    private void _verifyInitiated() {
        if (mContext == null) {
            throw new AssertionError("CommandDispatcher.init has not been called!");
        }
    }
}
