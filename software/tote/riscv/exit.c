
void _exit (int code) {
    (void)code;
    while (1);
}

void exit (int code) {
    _exit(code);
}