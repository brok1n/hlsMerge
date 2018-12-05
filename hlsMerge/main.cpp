#include "mainwindow.h"
#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    MainWindow w;
    w.workDir = a.applicationDirPath();
    w.show();

    return a.exec();
}
