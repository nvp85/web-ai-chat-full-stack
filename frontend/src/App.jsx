import { BrowserRouter, Route, Routes } from "react-router";
import Layout from "./components/Layout/Layout";
import SignIn from "./components/SignIn/SignIn";
import SignUp from "./components/SignUp/SignUp";
import HomePage from "./components/HomePage/HomePage";
import ChatPage from "./components/ChatPage/ChatPage";
import Profile from "./components/Profile/Profile";
import './App.css';
import AboutPage from "./components/AboutPage/AboutPage";
import ChatList from "./components/ChatList/ChatList";
import AuthProvider from "./contextProviders/AuthProvider";
import ProtectedRoutes from "./components/ProtectedRoutes/ProtectedRoutes";
import NotFound from "./components/NotFound";
import ChatListProvider from './contextProviders/ChatListProvider';
import RootErrorBoundary from "./errorBoundaries/RootErrorBoundary";



function App() {
    return (
        <RootErrorBoundary>
            <AuthProvider>
                <ChatListProvider>
                    <BrowserRouter>
                        <Routes>
                            <Route element={<Layout />}>
                                <Route path="login" element={<SignIn />} />
                                <Route path="register" element={<SignUp />} />
                                <Route path="about" element={<AboutPage />} />
                                <Route path="" element={<HomePage />} />

                                <Route element=<ProtectedRoutes /> >
                                    <Route path="chats/:id" element={<ChatPage />} />
                                    <Route path="chats" element={<ChatList />} />
                                    <Route path="profile" element={<Profile />} />
                                </Route>
                                <Route path="*" element={<NotFound />} />
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </ChatListProvider>
            </AuthProvider>
        </RootErrorBoundary>
    )

}

export default App;
